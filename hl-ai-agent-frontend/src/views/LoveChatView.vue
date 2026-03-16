<template>
  <ChatPage
    badge="Love App"
    title="AI 恋爱大师"
    description="适合分析聊天对象、设计话术与推进关系。该页面会在进入时自动生成 conversationId。"
    :conversation-id="conversationId"
    :messages="messages"
    :model-value="input"
    :loading="loading"
    :can-send="canSend"
    :error-message="errorMessage"
    @update:model-value="input = $event"
    @send="sendMessage"
    @reset="resetConversation"
  />
</template>

<script setup lang="ts">
import ChatPage from '../components/ChatPage.vue';
import { useChatStream } from '../composables/useChatStream';

const { canSend, conversationId, errorMessage, input, loading, messages, resetConversation, sendMessage } =
  useChatStream('love');
</script>

