package com.hl.hlaiagent.tools;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AskHumanToolTest {

    @Test
    void askHumanShouldReturnTrimmedHumanInput() {
        ByteArrayInputStream inputStream = new ByteArrayInputStream("  我来补充一下信息  \n".getBytes(StandardCharsets.UTF_8));
        AskHumanTool tool = new AskHumanTool(inputStream);

        String result = tool.askHuman("请补充缺失的信息");

        assertEquals("我来补充一下信息", result);
    }

    @Test
    void askHumanShouldRejectBlankInquiry() {
        ByteArrayInputStream inputStream = new ByteArrayInputStream("ignored\n".getBytes(StandardCharsets.UTF_8));
        AskHumanTool tool = new AskHumanTool(inputStream);

        String result = tool.askHuman("   ");

        assertEquals("Inquiry must not be blank.", result);
    }
}

