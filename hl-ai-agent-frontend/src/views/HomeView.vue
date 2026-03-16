<template>
  <div class="home-page shell-card">
    <section class="hero-panel">
      <p class="hero-eyebrow">HL AI Agent</p>
      <h1>选择你要使用的 AI 应用</h1>
      <p class="hero-description">
        主页用于切换不同应用，目前提供 AI 恋爱大师 与 AI 超级智能体 两种聊天能力。
      </p>
    </section>

    <section class="app-grid">
      <RouterLink class="app-card" to="/love-chat">
        <div>
          <p class="app-tag">页面 1</p>
          <h2>AI 恋爱大师</h2>
          <p>聊天室样式，自动生成 conversationId，通过 SSE 实时显示对话。</p>
        </div>
        <span class="app-link">进入应用 →</span>
      </RouterLink>

      <RouterLink class="app-card" to="/manus-chat">
        <div>
          <p class="app-tag">页面 2</p>
          <h2>AI 超级智能体</h2>
          <p>聊天室样式，调用 `doChatWithManus` 接口，实时展示流式返回内容。</p>
        </div>
        <span class="app-link">进入应用 →</span>
      </RouterLink>
    </section>
  </div>
</template>

<script setup lang="ts">
import { RouterLink } from 'vue-router';
</script>

