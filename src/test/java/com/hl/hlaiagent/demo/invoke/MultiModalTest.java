package com.hl.hlaiagent.demo.invoke;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class MultiModalTest {

    @Resource
    private MultiModal multiModal;

    @Test
    void simpleMultiModalConversationCall() {
        try {
            String response = multiModal.simpleMultiModalConversationCall();
            System.out.println("AI Response: " + response);
            assertNotNull(response);
            assertFalse(response.isEmpty());
        } catch (Exception e) {
            fail("Exception occurred during multi-modal conversation: " + e.getMessage());
        }
    }
}