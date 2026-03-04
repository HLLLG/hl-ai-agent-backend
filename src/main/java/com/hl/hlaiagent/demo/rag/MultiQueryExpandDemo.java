package com.hl.hlaiagent.demo.rag;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.rag.Query;
import org.springframework.ai.rag.preretrieval.query.expansion.MultiQueryExpander;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class MultiQueryExpandDemo {

    private final ChatClient.Builder chatClientBuilder;

    public MultiQueryExpandDemo(ChatModel dashScopeChatModel) {
        this.chatClientBuilder = ChatClient.builder(dashScopeChatModel);
    }

    public List<Query> expand(String query) {
        // 构建查询扩展器
        // 用于生成多个相关的查询变体，以获得更全面的搜索结果
        MultiQueryExpander queryExpander =
                MultiQueryExpander.builder().chatClientBuilder(chatClientBuilder).includeOriginal(false) // 不包含原始查询
                .numberOfQueries(3) // 生成3个查询变体
                .build();
        return queryExpander.expand(new Query(query));
    }
}
