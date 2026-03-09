package com.hl.hlaiagent.tools;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;

/**
 * 终端操作工具类，提供执行终端命令的功能。
 */
@Slf4j
public class TerminalOperationTool {

    private static final long TIMEOUT_SECONDS = 10;

    /**
     * 执行终端命令。
     *
     * @param command 要执行的命令
     * @return 命令执行结果
     */
    @Tool(description = "execute a terminal command")
    public String executeCommand(@ToolParam(description = "terminal command to execute") String command) {
        if (command == null || command.isBlank()) {
            return "Command must not be blank.";
        }

        try {
            // 使用 ProcessBuilder 执行命令
            Process process = new ProcessBuilder("cmd", "/c", command)
                    .redirectErrorStream(true)
                    .start();
            // 等待命令执行完成，设置超时时间
            boolean finished = process.waitFor(TIMEOUT_SECONDS, TimeUnit.SECONDS);
            if (!finished) {
                process.destroyForcibly();
                return "Command execution timed out.";
            }
            // 读取命令
            StringBuilder output = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    output.append(line).append(System.lineSeparator());
                }
            }

            return output.isEmpty()
                    ? "Command executed successfully with no output."
                    : output.toString().trim();
        } catch (Exception e) {
            log.error("Error executing terminal command: {}", command, e);
            return "Error executing terminal command: " + e.getMessage();
        }
    }
}
