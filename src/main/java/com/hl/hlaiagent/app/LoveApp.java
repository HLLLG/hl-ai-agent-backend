package com.hl.hlaiagent.app;

import com.hl.hlaiagent.advisor.MyLoggerAdvisor;
import com.hl.hlaiagent.advisor.ProhibitedWordAdvisor;
import com.hl.hlaiagent.advisor.ReReadingAdvisor;
import com.hl.hlaiagent.rag.LoveAppRagCustomAdvisorFactory;
import com.hl.hlaiagent.rag.QueryRewriter;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.api.Advisor;
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.chat.memory.repository.jdbc.JdbcChatMemoryRepository;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.mcp.SyncMcpToolCallbackProvider;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

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
//        ChatMemory chatMemory = MessageWindowChatMemory.builder()
//                .chatMemoryRepository(jdbcChatMemoryRepository)
//                .maxMessages(10)
//                .build();

        this.chatClient = ChatClient.builder(dashScopeChatModel)
                .defaultSystem(SYSTEM_PROMPT)
                .defaultAdvisors(MessageChatMemoryAdvisor.builder(MessageWindowChatMemory.builder().build()).build(),
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

    public Flux<String> doChatWithSse(String message, String conversationId) {
        Flux<String> fluxContent = chatClient
                .prompt()
                .user(message)
                .advisors(advisor -> advisor.param(ChatMemory.CONVERSATION_ID, conversationId))
                .stream()
                .content();
        return fluxContent;
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


    @Resource
    private VectorStore loveAppVectorStore;

    @Resource
    private Advisor loveAppRagCloudAdvisor;

//    @Resource
//    private VectorStore pgVectorStoreStore;

    @Resource
    private QueryRewriter queryRewriter;

    /**
     * AI 对话并结合 RAG（每次对话后结合检索到的相关文档内容，给出更专业的恋爱建议）
     * @param message
     * @param conversationId
     * @return
     */
    public String doChatWithRag(String message, String conversationId) {
        // 对用户输入的消息进行查询重写，以优化检索效果
        String rewrittenMessage = queryRewriter.rewriteQuery(message);

        ChatResponse chatResponse = chatClient
                .prompt()
                .user(rewrittenMessage)
                .advisors(advisor -> advisor.param(ChatMemory.CONVERSATION_ID, conversationId))
                // 结合 RAG 检索到的相关文档内容，给出更专业的恋爱建议
//                .advisors(QuestionAnswerAdvisor.builder(loveAppVectorStore).build())
                // 启用 RAG 检索增强服务（基于云知识库服务）
//                .advisors(loveAppRagCloudAdvisor)
                // 启用 RAG 检索增强服务（基于PGVector 向量存储）
//                .advisors(QuestionAnswerAdvisor.builder(pgVectorStoreStore).build())
                // 启用 RAG 检索增强服务（基于自定义检索器和查询增强器的本地向量存储）
                .advisors(LoveAppRagCustomAdvisorFactory.createCustomRagAdvisor(loveAppVectorStore, "单身"))
                .call()
                .chatResponse();
        return chatResponse.getResult().getOutput().getText();
    }


    @Resource
    private ToolCallback[] allTool;

    /**
     * AI 对话并生成恋爱报告（每次对话后生成恋爱结果，标题为{用户名}的恋爱报告，内容为建议列表）
     * @param message
     * @param conversationId
     * @return
     */
    public String doChatWithTools(String message, String conversationId) {
        ChatResponse chatResponse = chatClient
                .prompt()
                .user(message)
                .advisors(advisor -> advisor.param(ChatMemory.CONVERSATION_ID, conversationId))
                .toolCallbacks(allTool)
                .call()
                .chatResponse();
        return chatResponse.getResult().getOutput().getText();
    }

    @Resource
    private SyncMcpToolCallbackProvider toolCallbackProvider;

    /**
     * AI 对话并调用工具（根据用户输入的内容，智能调用相关工具获取信息或执行操作，提升恋爱建议的实用性）
     * @param message
     * @param conversationId
     * @return
     */
    public String doChatWithMcp(String message, String conversationId) {
        ChatResponse chatResponse = chatClient
                .prompt()
                .user(message)
                .advisors(advisor -> advisor.param(ChatMemory.CONVERSATION_ID, conversationId))
                .toolCallbacks(toolCallbackProvider)
                .call()
                .chatResponse();
        return chatResponse.getResult().getOutput().getText();
    }
}
