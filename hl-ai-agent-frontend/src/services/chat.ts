import { buildApiUrl } from './http';

export type StreamCallbacks = {
  onOpen?: () => void;
  onMessage: (chunk: string) => void;
  onError?: (message: string) => void;
  onComplete?: () => void;
};

export type StreamRequest = {
  path: string;
  params: Record<string, string>;
};

type StreamHandle = {
  close: () => void;
};

const DONE_MARKERS = new Set(['[DONE]', '__END__']);

function extractTextFromPayload(payload: string) {
  const trimmed = payload.trim();
  if (!trimmed) {
    return '';
  }

  if (DONE_MARKERS.has(trimmed)) {
    return trimmed;
  }

  try {
    const parsed = JSON.parse(trimmed) as unknown;

    if (typeof parsed === 'string') {
      return parsed;
    }

    if (parsed && typeof parsed === 'object') {
      const record = parsed as Record<string, unknown>;
      const candidate =
        record.content ??
        record.text ??
        record.delta ??
        record.message ??
        record.output ??
        record.result;

      if (typeof candidate === 'string') {
        return candidate;
      }
    }
  } catch {
    // Keep raw payload when it is not JSON.
  }

  return payload;
}

function emitEventBlock(block: string, onMessage: (chunk: string) => void, onComplete: () => void) {
  const lines = block.split(/\r?\n/);
  const dataLines: string[] = [];

  for (const line of lines) {
    if (!line || line.startsWith(':')) {
      continue;
    }

    if (line.startsWith('data:')) {
      dataLines.push(line.slice(5).trimStart());
      continue;
    }

    dataLines.push(line);
  }

  if (!dataLines.length) {
    return false;
  }

  const payload = extractTextFromPayload(dataLines.join('\n'));

  if (!payload) {
    return false;
  }

  if (DONE_MARKERS.has(payload.trim())) {
    onComplete();
    return true;
  }

  onMessage(payload);
  return false;
}

function emitPlainTextChunk(chunk: string, onMessage: (text: string) => void) {
  const payload = extractTextFromPayload(chunk);
  if (!payload || DONE_MARKERS.has(payload.trim())) {
    return;
  }

  onMessage(payload);
}

export function openSseStream(request: StreamRequest, callbacks: StreamCallbacks) {
  const url = buildApiUrl(`/ai${request.path}`, request.params);
  const controller = new AbortController();
  const decoder = new TextDecoder('utf-8');
  let finished = false;

  const complete = () => {
    if (finished) {
      return;
    }

    finished = true;
    callbacks.onComplete?.();
  };

  void (async () => {
    try {
      const response = await fetch(url, {
        method: 'GET',
        headers: {
          Accept: 'text/event-stream',
        },
        signal: controller.signal,
      });

      if (!response.ok) {
        callbacks.onError?.(`请求失败：${response.status}`);
        complete();
        return;
      }

      if (!response.body) {
        callbacks.onError?.('当前浏览器不支持流式响应读取。');
        complete();
        return;
      }

      callbacks.onOpen?.();

      const reader = response.body.getReader();
      let buffer = '';
      const isSse = response.headers.get('content-type')?.includes('text/event-stream') ?? false;

      while (true) {
        const { done, value } = await reader.read();

        if (done) {
          const finalBlock = buffer.trim();
          if (finalBlock) {
            emitEventBlock(finalBlock, callbacks.onMessage, complete);
          }
          complete();
          break;
        }

        buffer += decoder.decode(value, { stream: true });

        if (!isSse) {
          emitPlainTextChunk(buffer, callbacks.onMessage);
          buffer = '';
          continue;
        }

        const blocks = buffer.split(/\r?\n\r?\n/);
        buffer = blocks.pop() ?? '';

        for (const block of blocks) {
          const shouldStop = emitEventBlock(block, callbacks.onMessage, complete);
          if (shouldStop) {
            controller.abort();
            return;
          }
        }
      }
    } catch (error) {
      if (controller.signal.aborted) {
        return;
      }

      callbacks.onError?.(error instanceof Error ? error.message : '连接已中断，请检查后端服务是否启动。');
      complete();
    }
  })();

  const handle: StreamHandle = {
    close() {
      if (finished) {
        return;
      }

      finished = true;
      controller.abort();
    },
  };

  return handle;
}

