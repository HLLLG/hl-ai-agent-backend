<template>
  <ChatPage
    badge="Manus App"
    title="AI 超级智能体"
    description="适合执行复杂任务拆解与多步骤协作。该页面同样会自动生成本地会话 ID。"
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
  useChatStream('manus');
</script>

