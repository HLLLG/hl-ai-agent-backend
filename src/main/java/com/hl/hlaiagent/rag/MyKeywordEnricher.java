package com.hl.hlaiagent.rag;

import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.document.Document;
import org.springframework.ai.model.transformer.KeywordMetadataEnricher;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 基于 AI 模型的关键词增强器，用于从文档中提取关键词并将其作为元数据添加到文档中。
 */
@Component
class MyKeywordEnricher {

    private final ChatModel dashScopeChatModel;

    MyKeywordEnricher(ChatModel dashScopeChatModel) {
        this.dashScopeChatModel = dashScopeChatModel;
    }

    List<Document> enrichDocuments(List<Document> documents) {
        KeywordMetadataEnricher enricher = KeywordMetadataEnricher.builder(dashScopeChatModel)
                .keywordCount(5)
                .build();
        return enricher.apply(documents);
    }
}