package com.hl.hlaiagent.controller;

import com.hl.hlaiagent.agent.HlManus;
import com.hl.hlaiagent.app.LoveApp;
import com.hl.hlaiagent.common.BaseResponse;
import com.hl.hlaiagent.common.ResultUtils;
import jakarta.annotation.Resource;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import reactor.core.publisher.Flux;

@RestController
@RequestMapping("/ai")
public class AiController {

    @Resource
    private LoveApp loveApp;

    @Resource
    private ToolCallback[] allTools;

    @Resource
    private ChatModel dashscopeChatModel;

    @GetMapping("/love_app/chat/sync")
    public BaseResponse<String> loveAppChatSync(String message, String conversationId) {
        String result = loveApp.doChat(message, conversationId);
        return ResultUtils.success(result);
    }

    @GetMapping(value = "/love_app/chat/sse", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> loveAppChatSse(String message, String conversationId) {
        return loveApp.doChatWithSse(message, conversationId);
    }

    @GetMapping(value = "/love_app/chat/server_sent")
    public Flux<ServerSentEvent<String>> loveAppChatServerSent(String message, String conversationId) {
        return loveApp.doChatWithSse(message, conversationId)
                .map(chunk -> ServerSentEvent.<String>builder()
                        .data(chunk)
                        .build());
    }

    @GetMapping(value = "/love_app/chat/sse_emitter")
    public SseEmitter loveAppChatSseEmitter(String message, String conversationId) {
        SseEmitter emitter = new SseEmitter(180000L); // 设置超时时间为180秒
        // 通过SseEmitter发送数据
        loveApp.doChatWithSse(message, conversationId)
                .subscribe(
                        chunk -> {
                            try {
                                emitter.send(chunk);
                            } catch (Exception e) {
                                emitter.completeWithError(e);
                            }
                        },
                        emitter::completeWithError,
                        emitter::complete
                );
        return emitter;
    }

    @GetMapping("/agent/chat")
    public SseEmitter agentChat(String message) {
        HlManus hlManus = new HlManus(allTools, dashscopeChatModel);
        return  hlManus.runStream(message);
    }
}
