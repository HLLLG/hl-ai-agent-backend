package com.hl.hlaiagent.tools;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class WebSearchToolTest {

    @Value("${search-api.api-key}")
    private String API_KEY;

    @Test
    void searchWeb() {
        WebSearchTool webSearchTool = new WebSearchTool(API_KEY);
        String query = "Java编程语言";
        String result = webSearchTool.searchWeb(query);
        System.out.println(result);
        assertNotNull(result);
    }
}