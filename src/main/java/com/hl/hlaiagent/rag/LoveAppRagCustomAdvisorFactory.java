package com.hl.hlaiagent.rag;

import org.springframework.ai.chat.client.advisor.api.Advisor;
import org.springframework.ai.rag.advisor.RetrievalAugmentationAdvisor;
import org.springframework.ai.rag.retrieval.search.VectorStoreDocumentRetriever;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.filter.Filter;
import org.springframework.ai.vectorstore.filter.FilterExpressionBuilder;

/**
 * 自定义增强顾问工厂类，负责创建和配置与RAG相关的自定义顾问实例。
 */
public class LoveAppRagCustomAdvisorFactory {

    public static Advisor createCustomRagAdvisor(VectorStore vectorStore, String status) {

        // 构建过滤表达式，根据用户状态（单身、恋爱、已婚）过滤相关文档
        Filter.Expression expression = new FilterExpressionBuilder()
                .eq("status", status)
                .build();

        // 创建文档检索器
        VectorStoreDocumentRetriever documentRetriever = VectorStoreDocumentRetriever.builder()
                .vectorStore(vectorStore) // 设置向量数据库实例
                .similarityThreshold(0.6) // 设置相似度阈值，控制检索结果的相关性
                .topK(3) // 设置返回的文档数量
                .filterExpression(expression)
                .build();

        return RetrievalAugmentationAdvisor.builder()
                .documentRetriever(documentRetriever)
                .queryAugmenter(LoveAppContextualQueryAugmenterFactory.createInstance())
                .build();
    }
}
