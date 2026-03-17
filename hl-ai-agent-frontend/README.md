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

开发环境下，前端代码仍统一请求相对路径 `/api`，再由 Vite 开发服务器代理到本地后端 `localhost:8123`。

## 构建

```bash
npm run build
```

## 生产部署说明

- 生产构建后，前端会强制使用相对路径 `/api`
- 适合同域名部署，例如：前端页面与后端接口都挂在 `https://your-domain.com`
- 服务器需要将 `/api` 反向代理到 Spring Boot 服务

示例：

- 页面：`https://your-domain.com/`
- 接口：`https://your-domain.com/api/ai/...`

这样生产包中不会写入 `localhost` 地址。

## 说明

浏览器端原生 SSE 更适合使用 `EventSource` 消费流式返回；项目仍保留了 Axios 实例用于统一 API 基础配置与 URL 构造。

