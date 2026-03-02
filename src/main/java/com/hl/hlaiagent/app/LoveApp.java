package com.hl.hlaiagent.app;

import com.hl.hlaiagent.advisor.MyLoggerAdvisor;
import com.hl.hlaiagent.advisor.ProhibitedWordAdvisor;
import com.hl.hlaiagent.advisor.ReReadingAdvisor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.chat.memory.repository.jdbc.JdbcChatMemoryRepository;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 恋爱大师应用，提供恋爱心理咨询服务，支持单身、恋爱、已婚三种状态的用户，能够记忆多轮对话并生成个性化的恋爱建议报告。
 */
@Component
@Slf4j
public class LoveApp {

    private static final String SYSTEM_PROMPT = "扮演深耕恋爱心理领域的专家。开场向用户表明身份，告知用户可倾诉恋爱难题。" +
            "围绕单身、恋爱、已婚三种状态提问：单身状态询问社交圈拓展及追求心仪对象的困扰；" +
            "恋爱状态询问沟通、习惯差异引发的矛盾；已婚状态询问家庭责任与亲属关系处理的问题。" +
            "引导用户详述事情经过、对方反应及自身想法，以便给出专属解决方案。";


    private final ChatClient chatClient;

    /**
     * 构造方法，初始化ChatClient并设置系统提示和聊天记忆顾问
     * @param dashScopeChatModel 聊天模型
     * @param jdbcChatMemoryRepository JDBC聊天记忆仓库（自动注入）
     */
    public LoveApp(ChatModel dashScopeChatModel, JdbcChatMemoryRepository jdbcChatMemoryRepository) {
        // 使用 JDBC 持久化对话记忆到 MySQL
        ChatMemory chatMemory = MessageWindowChatMemory.builder()
                .chatMemoryRepository(jdbcChatMemoryRepository)
                .maxMessages(10)
                .build();

        this.chatClient = ChatClient.builder(dashScopeChatModel)
                .defaultSystem(SYSTEM_PROMPT)
                .defaultAdvisors(MessageChatMemoryAdvisor.builder(chatMemory).build(),
                        MyLoggerAdvisor.builder().order(0).build(),
                        new ReReadingAdvisor(),
                        ProhibitedWordAdvisor.builder().order(-1).build())
                .build();
    }

    /**
     * AI 基础对话（支持多轮对话记忆）
     * @param message
     * @param conversationId
     * @return
     */
    public String doChat(String message, String conversationId) {
        ChatResponse chatResponse = chatClient
                .prompt()
                .user(message)
                .advisors(advisor -> advisor.param(ChatMemory.CONVERSATION_ID, conversationId))
                .call()
                .chatResponse();
        String content = chatResponse.getResult().getOutput().getText();
        return content;
    }


    record LoveReport(String title, List<String> suggestions) {
    }

    /**
     * AI 对话并生成恋爱报告（每次对话后生成恋爱结果，标题为{用户名}的恋爱报告，内容为建议列表）
     * @param message
     * @param conversationId
     * @return
     */
    public LoveReport doChatWithReport(String message, String conversationId) {
        LoveReport loveReport = chatClient
                .prompt()
                .system(SYSTEM_PROMPT + "每次对话后都要生成恋爱结果，标题为{用户名}的恋爱报告，内容为建议列表")
                .user(message)
                .advisors(advisor -> advisor.param(ChatMemory.CONVERSATION_ID, conversationId))
                .call()
                .entity(LoveReport.class);
        return loveReport;
    }
}
