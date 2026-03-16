<template>
  <div class="chat-page shell-card">
    <header class="chat-header">
      <div class="chat-header__content">
        <p class="chat-eyebrow">{{ badge }}</p>
        <h1>{{ title }}</h1>
        <p class="chat-subtitle">{{ description }}</p>

        <div class="chat-meta-list">
          <span class="chat-meta-item">实时对话</span>
          <span class="chat-meta-item">流式输出</span>
          <span class="chat-meta-item">多端适配</span>
        </div>
      </div>

      <div class="chat-actions">
        <div class="conversation-card">
          <span class="conversation-label">当前会话 ID</span>
          <span class="conversation-id">{{ conversationId }}</span>
        </div>
        <button class="secondary-btn" type="button" @click="$emit('reset')">新会话</button>
        <RouterLink class="secondary-btn" to="/">返回首页</RouterLink>
      </div>
    </header>

    <main class="messages-panel">
      <div
        v-for="message in messages"
        :key="message.id"
        class="message-row"
        :class="message.role === 'user' ? 'is-user' : 'is-assistant'"
      >
        <div class="avatar">{{ message.role === 'user' ? '我' : 'AI' }}</div>
        <div class="bubble">
          <p class="message-role">{{ message.role === 'user' ? '用户' : '助手' }}</p>
          <p class="message-content">{{ message.content }}</p>
        </div>
      </div>
    </main>

    <footer class="composer-panel">
      <p v-if="errorMessage" class="status-text error-text">{{ errorMessage }}</p>
      <p v-else-if="loading" class="status-text">AI 正在实时回复中...</p>
      <div class="composer-row">
        <div class="composer-field">
          <textarea
            :value="modelValue"
            class="composer-input"
            rows="4"
            placeholder="请输入你的问题，按 Ctrl / Cmd + Enter 发送"
            @input="onInput"
            @keydown="onKeyDown"
          />
          <p class="composer-tips">支持 Ctrl / Cmd + Enter 快捷发送，适配桌面与移动端输入场景。</p>
        </div>
        <button class="primary-btn send-btn" type="button" :disabled="!canSend" @click="$emit('send')">
          {{ loading ? '生成中...' : '发送' }}
        </button>
      </div>
    </footer>
  </div>
</template>

<script setup lang="ts">
import { RouterLink } from 'vue-router';
import type { ChatMessage } from '../types/chat';

const props = defineProps<{
  badge: string;
  title: string;
  description: string;
  conversationId: string;
  messages: ChatMessage[];
  modelValue: string;
  loading: boolean;
  canSend: boolean;
  errorMessage: string;
}>();

const emit = defineEmits<{
  (e: 'update:modelValue', value: string): void;
  (e: 'send'): void;
  (e: 'reset'): void;
}>();

function onInput(event: Event) {
  const target = event.target as HTMLTextAreaElement;
  emit('update:modelValue', target.value);
}

function onKeyDown(event: KeyboardEvent) {
  if ((event.ctrlKey || event.metaKey) && event.key === 'Enter' && props.canSend) {
    emit('send');
  }
}
</script>

