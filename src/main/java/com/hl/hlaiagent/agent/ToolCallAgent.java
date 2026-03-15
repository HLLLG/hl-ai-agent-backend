package com.hl.hlaiagent.agent;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatOptions;
import com.hl.hlaiagent.advisor.MyLoggerAdvisor;
import com.hl.hlaiagent.agent.model.AgentState;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.ToolResponseMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.model.tool.ToolCallingManager;
import org.springframework.ai.model.tool.ToolExecutionResult;
import org.springframework.ai.tool.ToolCallback;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 工具调用代理类，继承自反应式代理类，定义了工具调用代理的特定行为和方法。工具调用代理在执行任务时会根据当前的输入和环境进行思考和行动，以实现更智能和灵活的工具调用行为。
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Slf4j
public class ToolCallAgent extends ReactAgent {

    // 可用工具列表，定义了代理可以调用的工具和方法
    private final ToolCallback[] availableTools;

    // 工具调用结果，定义了代理在调用工具后得到的结果和反馈
    private ChatResponse toolCallChatResponse;

    // 工具调用管理器，定义了代理在调用工具时的管理和调度机制
    private final ToolCallingManager toolCallingManager;

    // 聊天选项，定义了代理在进行工具调用时的聊天选项和配置
    private final ChatOptions chatOptions;

    public ToolCallAgent(ToolCallback[] availableTools) {
        super();
        this.availableTools = availableTools;
        this.toolCallingManager = ToolCallingManager.builder().build();
        this.chatOptions = DashScopeChatOptions.builder().build();
    }

    /**
     * 处理当前输入和环境，进行思考和决策，判断是否需要执行行动
     *
     * @return
     */
    @Override
    public boolean think() {
        if (StrUtil.isNotBlank(this.getNextStepPrompt())) {
            UserMessage usermessage = new UserMessage(this.getNextStepPrompt());
            this.getMessageList().add(usermessage);
        }
        List<Message> messageList = this.getMessageList();
        Prompt prompt = new Prompt(messageList, chatOptions);
        try {
            // 调用聊天模型进行工具调用，获取工具调用结果
            ChatResponse chatResponse =
                    this.getChatClient().prompt(prompt).system(this.getSystemPrompt()).toolCallbacks(availableTools).call().chatResponse();
            // 将工具调用结果保存到代理的状态中，以便后续的行动方法可以使用这些结果进行决策和操作
            this.toolCallChatResponse = chatResponse;
            AssistantMessage assistantMessage = chatResponse.getResult().getOutput();
            String result = assistantMessage.getText();
            log.info("{} 's thoughts: {}", this.getName(), result);
            List<AssistantMessage.ToolCall> toolCalls = assistantMessage.getToolCalls();
            log.info("{} selected {} tools to use", this.getName(), toolCalls.size());
            // 记录工具调用信息，包括工具名称和工具参数
            String toolCallInfo = toolCalls.stream().map(toolCall -> String.format("tool name: %s, tool " +
                    "arguments: %s", toolCall.name(), toolCall.arguments())).collect(Collectors.joining("\n"));
            log.info(toolCallInfo);
            if (toolCalls.isEmpty()) {
                // 如果没有工具调用，则将助手消息添加到消息列表中
                messageList.add(assistantMessage);
            } else {
                // 需要调用工具时，无需记录助手消息，因为调用工具时会自动记录工具调用的相关信息
                return true;
            }
        } catch (Exception e) {
            log.error("{} failed to think: {}", this.getName(), e.getMessage(), e);
            messageList.add(new AssistantMessage("Error during thinking: " + e.getMessage()));
            return false;
        }
        return false;
    }

    @Override
    public String act() {
        if (!toolCallChatResponse.hasToolCalls()) {
            return "No tools to call, no action needed";
        }
        // 调用工具
        Prompt prompt = new Prompt(this.getMessageList(), chatOptions);
        ToolExecutionResult toolExecutionResult = toolCallingManager.executeToolCalls(prompt, toolCallChatResponse);
        // 记录消息上下文，conversationHistory包含了助手消息喝工具调用返回结果
        setMessageList(toolExecutionResult.conversationHistory());
        // 当前工具的调用结果
        ToolResponseMessage toolResponseMessage =
                (ToolResponseMessage) CollUtil.getLast(toolExecutionResult.conversationHistory());
        String results =
                toolResponseMessage.getResponses().stream()
                        .map(toolResponse
                                -> String.format("tool name: " + "%s, " + "completed its mission! Result: " + "%s",
                                toolResponse.name(),
                                toolResponse.responseData()))
                        .collect(Collectors.joining("\n"));
        // 判断是否调用了终止工具
        boolean hasTerminationTool =
                toolResponseMessage.getResponses().stream().anyMatch(toolCall -> toolCall.name().equals("doTerminate"));
        if (hasTerminationTool) {
            setState(AgentState.FINISHED);
        }
        log.info(results);
        return results;
    }
}
