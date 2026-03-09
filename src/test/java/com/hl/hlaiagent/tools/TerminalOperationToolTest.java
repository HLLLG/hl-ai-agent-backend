package com.hl.hlaiagent.tools;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TerminalOperationToolTest {

    @Test void executeCommand() {
        TerminalOperationTool tool = new TerminalOperationTool();

        String result = tool.executeCommand("echo Hello, World!");
        assertEquals("Hello, World!", result);

        result = tool.executeCommand("");
        assertEquals("Command must not be blank.", result);

        result = tool.executeCommand(null);
        assertEquals("Command must not be blank.", result);
    }
}
