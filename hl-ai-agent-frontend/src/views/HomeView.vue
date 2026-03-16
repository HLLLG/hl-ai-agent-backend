<template>
  <div class="home-page shell-card">
    <section class="hero-panel">
      <div class="hero-copy">
        <p class="hero-eyebrow">HL AI Agent</p>
        <h1>统一风格的 AI 应用门户</h1>
        <p class="hero-description">
          在一个界面中快速切换不同 AI 场景：恋爱沟通辅助、任务拆解与智能体协作。
          页面统一采用深色科技风格，并适配桌面、平板与手机。
        </p>

        <div class="hero-actions">
          <RouterLink class="primary-btn" to="/love-chat">立即体验恋爱大师</RouterLink>
          <RouterLink class="secondary-btn" to="/manus-chat">打开超级智能体</RouterLink>
        </div>
      </div>

      <div class="hero-highlight">
        <div class="hero-stat-card">
          <span class="hero-stat-label">应用数量</span>
          <strong>2</strong>
          <p>覆盖情感沟通与复杂任务处理两个典型 AI 使用场景。</p>
        </div>
        <div class="hero-stat-card">
          <span class="hero-stat-label">交互方式</span>
          <strong>SSE 实时响应</strong>
          <p>对话内容按流式返回渲染，提供更自然的聊天式反馈体验。</p>
        </div>
      </div>
    </section>

    <section class="home-section">
      <div class="section-heading">
        <div>
          <p class="app-tag">Applications</p>
          <h2>选择一个 AI 能力开始使用</h2>
        </div>
        <p>每个应用都带有独立会话上下文与统一的聊天交互界面。</p>
      </div>

      <section class="app-grid">
        <RouterLink class="app-card" to="/love-chat">
          <div>
            <p class="app-tag">页面 1</p>
            <h2>AI 恋爱大师</h2>
            <p>自动生成 conversationId，通过 SSE 实时返回内容，适合分析话术、推进关系与整理沟通策略。</p>
          </div>
          <div class="app-card-footer">
            <span class="app-chip">会话记忆</span>
            <span class="app-link">进入应用 →</span>
          </div>
        </RouterLink>

        <RouterLink class="app-card" to="/manus-chat">
          <div>
            <p class="app-tag">页面 2</p>
            <h2>AI 超级智能体</h2>
            <p>适合多步骤任务执行，支持按 Step 拆分显示结果，让规划与执行过程更加清晰易读。</p>
          </div>
          <div class="app-card-footer">
            <span class="app-chip">Step 拆分</span>
            <span class="app-link">进入应用 →</span>
          </div>
        </RouterLink>
      </section>
    </section>

    <section class="home-section feature-grid">
      <article class="feature-card">
        <p class="app-tag">统一体验</p>
        <h3>统一视觉风格</h3>
        <p>全站使用统一配色、圆角、阴影和内容宽度规范，提升整体一致性与专业感。</p>
      </article>

      <article class="feature-card">
        <p class="app-tag">响应式设计</p>
        <h3>兼容多端设备</h3>
        <p>桌面端强调效率布局，平板端优化卡片密度，手机端聚焦单列阅读和输入操作体验。</p>
      </article>

      <article class="feature-card">
        <p class="app-tag">实时反馈</p>
        <h3>流式对话更自然</h3>
        <p>聊天内容与任务步骤会按服务端流式返回逐步展示，减少等待感，强化交互节奏。</p>
      </article>
    </section>
  </div>
</template>

<script setup lang="ts">
import { RouterLink } from 'vue-router';
</script>

