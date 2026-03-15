package com.hl.hlaiagent.tools;

import org.springframework.ai.support.ToolCallbacks;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;

/**
 * 工具注册类，负责将所有工具注册到Spring上下文中，以便AI代理能够使用这些工具。
 */
@Configuration
public class ToolRegistration {

    @Value("${search-api.api-key}")
    private String apiKey;

    @Value("${spring.mail.username}")
    private String mailUsername;

    @Bean public ToolCallback[] allTools(JavaMailSender mailSender) {
        FileOperationTool fileOperationTool = new FileOperationTool();
        WebSearchTool webSearchTool = new WebSearchTool(apiKey);
        WebScrapingTool webScrapingTool = new WebScrapingTool();
        ResourceDownloadTool resourceDownloadTool = new ResourceDownloadTool();
        TerminalOperationTool terminalOperationTool = new TerminalOperationTool();
        PDFGenerationTool pdfGenerationTool = new PDFGenerationTool();
        QQMailSendTool qqMailSendTool = new QQMailSendTool(mailSender, mailUsername);
        AskHumanTool askHumanTool = new AskHumanTool();
        TerminateTool terminateTool = new TerminateTool();

        return ToolCallbacks.from(
                fileOperationTool,
                webSearchTool,
                webScrapingTool,
                resourceDownloadTool,
                terminalOperationTool,
                pdfGenerationTool,
                qqMailSendTool,
                askHumanTool,
                terminateTool);
    }
}
