package com.hl.hlaiagent.rag;

import jakarta.annotation.Resource;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class PgVectorStoreConfigTest {

    @Resource
    private VectorStore pgVectorStoreStore;

    @Test
    void test() {
        // Retrieve documents similar to a query
        List<Document> results =
                this.pgVectorStoreStore.similaritySearch(SearchRequest.builder().query("String message = \"你好，我是程序员HL，我想让另一半(卿卿子)更爱我，但我不知道该怎么做\";").topK(5).build());
        Assertions.assertNotNull(results);
    }
}