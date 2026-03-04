package com.hl.hlaiagent.demo.rag;

import jakarta.annotation.Resource;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.ai.rag.Query;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class MultiQueryExpandDemoTest {

    @Resource
    private MultiQueryExpandDemo multiQueryExpandDemo;

    @Test
    void expand() {
        List<Query> queries = multiQueryExpandDemo.expand("程序员HL是谁");
        Assertions.assertFalse(queries.isEmpty());
    }
}