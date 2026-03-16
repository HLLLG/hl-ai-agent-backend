import { computed, onBeforeUnmount, ref, watch } from 'vue';
import { openSseStream } from '../services/chat';
import type { ChatMessage } from '../types/chat';

export type ChatMode = 'love' | 'manus';

type StreamHandle = {
  close: () => void;
};

type StoredChatState = {
  conversationId: string;
  messages: ChatMessage[];
};

function getWelcomeMessage(mode: ChatMode, isReset = false): ChatMessage {
  return {
    id: `${mode}-${isReset ? 'reset' : 'welcome'}`,
    role: 'assistant',
    content:
      mode === 'love'
        ? isReset
          ? '已创建新的恋爱咨询会话，你可以继续描述你的情况。'
          : '你好，我是 AI 恋爱大师，可以帮你分析聊天对象、优化话术和推进关系。'
        : isReset
          ? '已创建新的智能体会话，请继续输入新的任务。'
          : '你好，我是 AI 超级智能体，请告诉我你的任务目标，我会一步步协助你完成。',
  };
}

function getStorageKey(mode: ChatMode) {
  return `hl-ai-agent:${mode}:chat-state`;
}

function isChatMessage(value: unknown): value is ChatMessage {
  if (!value || typeof value !== 'object') {
    return false;
  }

  const message = value as Partial<ChatMessage>;
  return typeof message.id === 'string' && typeof message.role === 'string' && typeof message.content === 'string';
}

function loadStoredState(mode: ChatMode): StoredChatState | null {
  if (typeof window === 'undefined') {
    return null;
  }

  const raw = window.sessionStorage.getItem(getStorageKey(mode));
  if (!raw) {
    return null;
  }

  try {
    const parsed = JSON.parse(raw) as Partial<StoredChatState>;
    if (typeof parsed.conversationId !== 'string' || !Array.isArray(parsed.messages)) {
      return null;
    }

    const messages = parsed.messages.filter(isChatMessage);
    if (!messages.length) {
      return null;
    }

    return {
      conversationId: parsed.conversationId,
      messages,
    };
  } catch {
    return null;
  }
}

function persistState(mode: ChatMode, state: StoredChatState) {
  if (typeof window === 'undefined') {
    return;
  }

  window.sessionStorage.setItem(getStorageKey(mode), JSON.stringify(state));
}

function generateConversationId() {
  if (typeof crypto !== 'undefined' && typeof crypto.randomUUID === 'function') {
    return crypto.randomUUID();
  }

  return `chat-${Date.now()}-${Math.random().toString(16).slice(2, 10)}`;
}

function splitManusResponseIntoBubbles(content: string) {
  if (!content) {
    return [] as string[];
  }

  const stepRegex = /Step\s+\d+\b/g;
  const matches = [...content.matchAll(stepRegex)];

  if (!matches.length) {
    return [content];
  }

  const segments: string[] = [];
  const firstStepIndex = matches[0].index ?? 0;
  const prefix = content.slice(0, firstStepIndex);

  if (prefix.trim()) {
    segments.push(prefix);
  }

  for (let index = 0; index < matches.length; index += 1) {
    const start = matches[index].index ?? 0;
    const end = index + 1 < matches.length ? (matches[index + 1].index ?? content.length) : content.length;
    const segment = content.slice(start, end);

    if (segment.trim()) {
      segments.push(segment);
    }
  }

  return segments;
}

export function useChatStream(mode: ChatMode) {
  const storedState = loadStoredState(mode);
  const conversationId = ref(storedState?.conversationId ?? generateConversationId());
  const messages = ref<ChatMessage[]>(storedState?.messages ?? [getWelcomeMessage(mode)]);
  const input = ref('');
  const loading = ref(false);
  const errorMessage = ref('');
  let streamHandle: StreamHandle | null = null;
  let activeRequestId = 0;

  const canSend = computed(() => input.value.trim().length > 0 && !loading.value);

  function closeCurrentStream() {
    streamHandle?.close();
    streamHandle = null;
    activeRequestId += 1;
    loading.value = false;
  }

  function createMessage(role: ChatMessage['role'], content = ''): ChatMessage {
    return {
      id: `${role}-${Date.now()}-${Math.random().toString(16).slice(2, 8)}`,
      role,
      content,
    };
  }

  function appendToMessage(index: number, chunk: string) {
    const currentMessage = messages.value[index];
    if (!currentMessage) {
      return;
    }

    messages.value[index] = {
      ...currentMessage,
      content: currentMessage.content + chunk,
    };
  }

  function replaceMessageContent(index: number, content: string) {
    const currentMessage = messages.value[index];
    if (!currentMessage) {
      return;
    }

    messages.value[index] = {
      ...currentMessage,
      content,
    };
  }

  function syncAssistantMessages(startIndex: number, segments: string[], messageIds: string[]) {
    while (messageIds.length < segments.length) {
      messageIds.push(createMessage('assistant').id);
    }

    const prefixMessages = messages.value.slice(0, startIndex);
    const assistantMessages = segments.map((content, index) => ({
      id: messageIds[index],
      role: 'assistant' as const,
      content,
    }));

    messages.value = [...prefixMessages, ...assistantMessages];
  }

  function resetConversation() {
    closeCurrentStream();
    conversationId.value = generateConversationId();
    errorMessage.value = '';
    messages.value = [getWelcomeMessage(mode, true)];
    input.value = '';
  }

  function sendMessage() {
    const text = input.value.trim();
    if (!text || loading.value) {
      return;
    }

    errorMessage.value = '';
    closeCurrentStream();

    messages.value.push(createMessage('user', text));
    const assistantMessage = createMessage('assistant');
    const responseStartIndex = messages.value.length;
    let assistantMessageIndex = -1;
    let manusResponseText = '';
    const manusMessageIds: string[] = [];

    if (mode === 'love') {
      messages.value.push(assistantMessage);
      assistantMessageIndex = messages.value.length - 1;
    }

    input.value = '';
    loading.value = true;
    const requestId = activeRequestId + 1;
    activeRequestId = requestId;

    streamHandle = openSseStream(
      mode === 'love'
        ? {
            path: '/love_app/chat/sse',
            params: {
              message: text,
              conversationId: conversationId.value,
            },
          }
        : {
            path: '/manus/chat',
            params: {
              message: text,
            },
          },
      {
        onMessage(chunk) {
          if (requestId !== activeRequestId) {
            return;
          }

          if (mode === 'manus') {
            manusResponseText += chunk;
            const segments = splitManusResponseIntoBubbles(manusResponseText);
            syncAssistantMessages(responseStartIndex, segments, manusMessageIds);
            return;
          }

          appendToMessage(assistantMessageIndex, chunk);
        },
        onError(message) {
          if (requestId !== activeRequestId) {
            return;
          }
          errorMessage.value = message;

          if (mode === 'manus') {
            if (!manusResponseText.trim()) {
              syncAssistantMessages(responseStartIndex, ['暂时没有收到服务端内容。'], manusMessageIds);
            }
            return;
          }

          if (!messages.value[assistantMessageIndex]?.content) {
            replaceMessageContent(assistantMessageIndex, '暂时没有收到服务端内容。');
          }
        },
        onComplete() {
          if (requestId !== activeRequestId) {
            return;
          }
          loading.value = false;
          streamHandle = null;
        },
      },
    );
  }

  onBeforeUnmount(() => {
    closeCurrentStream();
  });

  watch(
    [conversationId, messages],
    () => {
      persistState(mode, {
        conversationId: conversationId.value,
        messages: messages.value,
      });
    },
    {
      deep: true,
      immediate: true,
    },
  );

  return {
    canSend,
    conversationId,
    errorMessage,
    input,
    loading,
    messages,
    resetConversation,
    sendMessage,
  };
}

