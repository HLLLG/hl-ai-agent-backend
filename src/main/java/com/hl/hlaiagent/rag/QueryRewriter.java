package com.hl.hlaiagent.rag;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.rag.Query;
import org.springframework.ai.rag.preretrieval.query.transformation.RewriteQueryTransformer;
import org.springframework.stereotype.Component;


/**
 * QueryRewriter 负责对用户输入的查询进行重写，以优化查询效果和提高检索相关性。
 */
@Component
public class QueryRewriter {

    private ChatClient.Builder chatClientBuilder;

    public QueryRewriter(ChatModel dashScopeChatModel) {
        this.chatClientBuilder = ChatClient.builder(dashScopeChatModel);
    }

    public String rewriteQuery(String message) {
        Query query = new Query(message);

        // 创建查询重写转换器
        RewriteQueryTransformer rewriteQueryTransformer = RewriteQueryTransformer.builder()
                .chatClientBuilder(chatClientBuilder)
                .build();

        // 执行查询重写
        Query transformQuery = rewriteQueryTransformer.transform(query);
        return transformQuery.text();

    }
}
