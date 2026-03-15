package com.hl.hlaiagent.agent;

import com.hl.hlaiagent.advisor.MyLoggerAdvisor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.stereotype.Component;

/**
 * HlManus代理类，继承自工具调用代理类，定义了YuManus代理的特定行为和方法。
 * HlManus代理是一个全能的AI助手，旨在解决用户提出的任何任务。
 * 它拥有各种工具，可以高效地完成复杂的请求，并且能够根据用户需求主动选择最合适的工具或工具组合来完成任务。
 */
@Component
public class HlManus extends ToolCallAgent {

    public HlManus(ToolCallback[] allTools, ChatModel dashscopeChatModel) {
        super(allTools);
        this.setName("HlManus");
        String SYSTEM_PROMPT = """  
                You are YuManus, an all-capable AI assistant, aimed at solving any task presented by the user.  
                You have various tools at your disposal that you can call upon to efficiently complete complex requests.  
                """;
        this.setSystemPrompt(SYSTEM_PROMPT);
        String NEXT_STEP_PROMPT = """  
                Based on user needs, proactively select the most appropriate tool or combination of tools.  
                For complex tasks, you can break down the problem and use different tools step by step to solve it.  
                After using each tool, clearly explain the execution results and suggest the next steps.  
                If you want to stop the interaction at any point, use the `terminate` tool/function call.  
                """;
        this.setNextStepPrompt(NEXT_STEP_PROMPT);
        this.setMaxSteps(20);
        // 初始化客户端
        ChatClient chatClient = ChatClient.builder(dashscopeChatModel).defaultAdvisors(new MyLoggerAdvisor()).build();
        this.setChatClient(chatClient);
    }
}
