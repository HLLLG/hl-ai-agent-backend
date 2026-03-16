<template>
  <div class="app-shell">
    <div class="app-bg app-bg-primary"></div>
    <div class="app-bg app-bg-secondary"></div>

    <main class="app-main">
      <RouterView />
    </main>

    <footer class="site-footer">
      <div class="site-footer__inner">
        <div>
          <p class="site-footer__brand">HL AI Agent</p>
          <p class="site-footer__text">智能应用体验站点，面向 AI 恋爱咨询与 AI 超级智能体交互场景。</p>
        </div>

        <div class="site-footer__meta">
          <span>© 2026 HL AI Agent. All Rights Reserved.</span>
          <span>Designed for Web · Responsive for Desktop / Tablet / Mobile</span>
        </div>
      </div>
    </footer>
  </div>
</template>

<script setup lang="ts">
import { RouterView } from 'vue-router';
</script>

