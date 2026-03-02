package com.hl.hlaiagent.demo.invoke;

import java.net.MalformedURLException;
import java.net.URI;

import com.hl.hlaiagent.advisor.MyLoggerAdvisor;
import com.hl.hlaiagent.advisor.ProhibitedWordAdvisor;
import com.hl.hlaiagent.advisor.ReReadingAdvisor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.chat.memory.repository.jdbc.JdbcChatMemoryRepository;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.MimeTypeUtils;

@Component
public class MultiModal {

    private final ChatClient chatClient;

    public MultiModal(ChatModel dashScopeChatModel, JdbcChatMemoryRepository jdbcChatMemoryRepository) {
        // 使用 JDBC 持久化对话记忆到 MySQL
        ChatMemory chatMemory = MessageWindowChatMemory.builder()
                .chatMemoryRepository(jdbcChatMemoryRepository)
                .maxMessages(10)
                .build();

        this.chatClient = ChatClient.builder(dashScopeChatModel)
                .defaultSystem("你是一个多模态智能体，能够理解和处理文本、图片等多种类型的输入。")
                .defaultAdvisors(MessageChatMemoryAdvisor.builder(chatMemory).build(),
                        MyLoggerAdvisor.builder().order(0).build(),
                        new ReReadingAdvisor(),
                        ProhibitedWordAdvisor.builder().order(-1).build())
                .build();
    }

    public String simpleMultiModalConversationCall() throws MalformedURLException {

        ChatResponse chatResponse = chatClient
                .prompt()
                .user(u -> {
                    try {
                        u.text("这些是什么?")
                         .media(MimeTypeUtils.IMAGE_JPEG,
                                 URI.create("https://dashscope.oss-cn-beijing.aliyuncs.com/images/dog_and_girl.jpeg").toURL());
                    } catch (MalformedURLException e) {
                        throw new RuntimeException(e);
                    }
                })
                .advisors(advisor -> advisor.param(ChatMemory.CONVERSATION_ID, "multi-modal-conversation-001"))
                .call()
                .chatResponse();
        return chatResponse.getResult().getOutput().getText();
    }

}