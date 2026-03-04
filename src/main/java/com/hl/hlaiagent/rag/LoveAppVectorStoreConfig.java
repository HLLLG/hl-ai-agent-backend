package com.hl.hlaiagent.rag;

import com.alibaba.cloud.ai.dashscope.embedding.DashScopeEmbeddingModel;
import jakarta.annotation.Resource;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * 恋爱大师向量数据库配置（初始化基于内存的向量数据库 Bean）
 */
@Configuration
public class LoveAppVectorStoreConfig {

    @Resource
    private LoveAppDocumentLoader loveAppDocumentLoader;

    @Resource
    private MyKeywordEnricher myKeywordEnricher;

    @Bean
    VectorStore loveAppVectorStore(DashScopeEmbeddingModel dashScopeEmbeddingModel) {
        // 加载文档
        List<Document> documents = loveAppDocumentLoader.loadDocuments();
        // 创建基于内存的向量数据库
        SimpleVectorStore simpleVectorStore = SimpleVectorStore.builder(dashScopeEmbeddingModel).build();
        // 对文档进行关键词增强
        List<Document> enrichDocuments = myKeywordEnricher.enrichDocuments(documents);
        // 将文档添加到向量数据库中
        simpleVectorStore.doAdd(enrichDocuments);
        return simpleVectorStore;
    }
}
