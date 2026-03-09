package com.hl.hlaiagent.tools;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class FileOperationToolTest {

    @Test
    void readFile() {
        String content = new FileOperationTool().readFile("test.txt");
        assertNotNull(content);
    }

    @Test
    void writeFile() {
        new FileOperationTool().writeFile("test.txt", "Hello, World!");
    }
}