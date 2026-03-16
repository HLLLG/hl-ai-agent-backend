# hl-ai-agent-frontend

基于 Vue 3 + Vite + TypeScript 的前端示例，包含：

- 首页：切换到不同 AI 应用
- `AI 恋爱大师` 页面：自动生成 `conversationId`，调用 `/api/ai/love_app/chat/sse`
- `AI 超级智能体` 页面：调用 `/api/ai/manus/chat`

## 本地启动

```bash
npm install
npm run dev
```

开发环境默认地址：`http://localhost:5173`

## 后端联调

前端通过 `vite.config.ts` 里的代理把 `/api` 转发到：

- `http://localhost:8123`

因此浏览器实际访问的接口为：

- `/api/ai/love_app/chat/sse?message=xxx&conversationId=xxx`
- `/api/ai/manus/chat?message=xxx`

## 构建

```bash
npm run build
```

## 说明

浏览器端原生 SSE 更适合使用 `EventSource` 消费流式返回；项目仍保留了 Axios 实例用于统一 API 基础配置与 URL 构造。

