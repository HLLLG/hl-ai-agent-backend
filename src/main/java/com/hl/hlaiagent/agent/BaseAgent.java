package com.hl.hlaiagent.agent;

import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.StrUtil;
import com.hl.hlaiagent.agent.model.AgentState;
import com.hl.hlaiagent.exception.ErrorCode;
import com.hl.hlaiagent.exception.ThrowUtils;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.MessageType;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.web.context.request.async.AsyncRequestNotUsableException;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * 基础代理类，定义了代理的基本属性和方法。所有具体的代理类都应该继承这个基础类，并实现其抽象方法，以便在不同的场景下执行特定的任务。
 */
@Slf4j
@Data
public abstract class BaseAgent {

    // 代理名称，用于标识不同的代理实例
    private String name;

    // 系统提示，定义了代理在执行任务时的行为和指导原则
    private String systemPrompt;

    // 下一步提示，指导代理在完成当前任务后应该执行的下一步操作
    private String nextStepPrompt;

    // llm
    private ChatClient chatClient;

    // memory 代理的记忆列表，用于存储与用户的对话历史和相关信息，以便在后续的交互中参考和使用
    private List<Message> messageList = new ArrayList<>();

    // 最大步骤数，限制代理在执行任务时的最大步骤数，以防止无限循环或过长的执行过程
    private int maxSteps = 10;

    // 当前步骤数，记录代理在执行任务时已经执行的步骤数，以便在达到最大步骤数时停止执行
    private int currentStep = 0;

    // 代理状态，表示代理当前的状态，如空闲、执行中、等待输入等，以便在不同状态下执行不同的行为和处理逻辑
    private AgentState state = AgentState.IDLE;

    // 重复阈值，定义了代理在执行任务时判断是否陷入重复循环的阈值，以便在达到该阈值时采取相应的措施，如终止执行或提示用户
    private final int duplicateThreshold = 2;
    private static final long STREAM_TIMEOUT_MILLIS = 180000L;
    private final Object runLock = new Object();

    /**
     * 单步执行方法，定义了代理在每一步执行时的具体操作和逻辑。
     * @return
     */
    public abstract String step();

    /**
     * 清理方法，在代理执行完成或发生错误时调用，用于清理代理的状态和资源，以便为下一次执行做好准备。
     */
    protected void cleanup() {
        synchronized (runLock) {
            this.messageList.clear();
            this.currentStep = 0;
            this.state = AgentState.IDLE;
        }
    }

    /**
     * 运行代理
     * @param userPrompt
     * @return
     */
    public String run(String userPrompt) {
        synchronized (runLock) {
            ThrowUtils.throwIf(this.state != AgentState.IDLE, ErrorCode.OPERATION_ERROR, "Agent is not idle");
            ThrowUtils.throwIf(StrUtil.isBlank(userPrompt), ErrorCode.OPERATION_ERROR, "userPrompt is blank");
            this.state = AgentState.RUNNING;
            this.currentStep = 0;
            this.messageList.add(new UserMessage(userPrompt));
        }

        List<String> stepResults = new ArrayList<>();
        try {
            // 执行步骤，直到达到最大步骤数或代理状态不再为执行中
            for (int i = 0; i < this.maxSteps && this.state == AgentState.RUNNING; i++) {
                int stepNum = i + 1;
                this.currentStep = stepNum;
                log.info("Executing currentStep {} / maxStep {}", currentStep, maxSteps);

                String stepResult = this.step();
                if (isStuck()) {
                    handleStuckState();
                    stepResult += "\nAgent is stuck in a loop, added stuck prompt to message list";
                }
                stepResults.add(String.format("Step %d stepResult: %s", stepNum, stepResult));
            }

            if (stepResults.isEmpty()) {
                log.warn("Agent {} run with no steps executed", this.name);
                return "";
            }

            if (this.state == AgentState.RUNNING && this.currentStep >= this.maxSteps) {
                this.state = AgentState.FINISHED;
                stepResults.add(String.format("Terminated: Reached max steps %s", this.maxSteps));
                log.warn("Agent {} run reached max steps {} with state {}", this.name, this.maxSteps, this.state);
            }
            return String.join("\n", stepResults);
        } catch (Exception e) {
            this.state = AgentState.ERROR;
            log.error("Agent {} run failed with exception", this.name, e);
            return "Agent " + this.name + " run error: " + e.getMessage();
        } finally {
            this.cleanup();
        }
    }

    /**
     * 流式运行代理
     * @param userPrompt
     * @return
     */
    public SseEmitter runStream(String userPrompt) {
        SseEmitter emitter = new SseEmitter(STREAM_TIMEOUT_MILLIS);

        emitter.onTimeout(() -> handleStreamLifecycle("timed out", AgentState.FINISHED));
        emitter.onCompletion(() -> {
            handleStreamLifecycle("completed", AgentState.FINISHED);
            log.info("Agent {} runStream completed", this.name);
        });
        emitter.onError(error -> {
            log.error("Agent {} runStream error", this.name, error);
            handleStreamLifecycle("errored", AgentState.ERROR);
        });

        CompletableFuture.runAsync(() -> doRunStream(userPrompt, emitter));
        return emitter;
    }

    private void doRunStream(String userPrompt, SseEmitter emitter) {
        if (!initStreamRun(userPrompt, emitter)) {
            return;
        }

        List<String> stepResults = new ArrayList<>();
        try {
            for (int i = 0; i < this.maxSteps && this.state == AgentState.RUNNING; i++) {
                int stepNum = i + 1;
                this.currentStep = stepNum;
                log.info("Executing currentStep {} / maxStep {}", currentStep, maxSteps);

                String stepResult = this.step();
                if (isStuck()) {
                    handleStuckState();
                    stepResult += "\nAgent is stuck in a loop, added stuck prompt to message list";
                }

                String message = String.format("Step %d stepResult: %s", stepNum, stepResult);
                stepResults.add(message);
                if (!trySendStreamMessage(emitter, message)) {
                    return;
                }
            }

            if (stepResults.isEmpty()) {
                log.warn("Agent {} run with no steps executed", this.name);
                trySendStreamMessage(emitter, String.format("Agent {} run with no steps executed", this.name));
                return;
            }

            if (this.state == AgentState.RUNNING && this.currentStep >= this.maxSteps) {
                this.state = AgentState.FINISHED;
                String message = String.format("Terminated: Reached max steps %s", this.maxSteps);
                stepResults.add(message);
                log.warn("Agent {} run reached max steps {} with state {}", this.name, this.maxSteps, this.state);
                trySendStreamMessage(emitter, message);
            }
        } catch (Exception e) {
            this.state = AgentState.ERROR;
            log.error("Agent {} run failed with exception", this.name, e);
            if (!trySendStreamMessage(emitter, "Agent " + this.name + " run error: " + e.getMessage())) {
                emitter.completeWithError(e);
                return;
            }
        } finally {
            emitter.complete();
            this.cleanup();
        }
    }

    private boolean initStreamRun(String userPrompt, SseEmitter emitter) {
        synchronized (runLock) {
            if (this.state != AgentState.IDLE) {
                trySendStreamMessage(emitter, "错误，代理当前状态 " + this.state + " 不允许执行新的任务");
                emitter.complete();
                return false;
            }
            if (StrUtil.isBlank(userPrompt)) {
                trySendStreamMessage(emitter, "错误，用户输入不能为空");
                emitter.complete();
                return false;
            }
            this.state = AgentState.RUNNING;
            this.currentStep = 0;
            this.messageList.add(new UserMessage(userPrompt));
            return true;
        }
    }

    private boolean trySendStreamMessage(SseEmitter emitter, String message) {
        try {
            emitter.send(SseEmitter.event().data(message));
            return true;
        } catch (AsyncRequestNotUsableException | IllegalStateException e) {
            log.debug("SSE client disconnected: {}", e.getMessage());
            try {
                emitter.complete();
            } catch (Exception ignored) {
            }
            return false;
        } catch (IOException e) {
            log.warn("SSE send failed: {}", e.getMessage());
            try {
                emitter.completeWithError(e);
            } catch (Exception ignored) {
            }
            return false;
        }
    }

    private void handleStreamLifecycle(String event, AgentState targetState) {
        synchronized (runLock) {
            if (this.state == AgentState.RUNNING) {
                log.warn("Agent {} runStream {} with state still RUNNING, setting state to {}", this.name, event, targetState);
                this.state = targetState;
            } else {
                log.info("Agent {} runStream {} with state {}", this.name, event, this.state);
            }
        }
    }

    /**
     * 检查代理是否陷入重复循环，通过比较记忆列表中最近的消息，判断是否存在连续的重复消息，如果存在超过设定的重复阈值，则认为代理陷入了重复循环。
     * @return
     */
    private boolean isStuck() {
        if (this.messageList.size() < 2) {
            return false;
        }

        Message lastMessage = this.messageList.get(this.messageList.size() - 1);
        if (ObjUtil.isEmpty(lastMessage)) {
            return false;
        }

        int duplicateCount = 0;
        for (int i = this.messageList.size() - 2; i >= 0; i--) {
            Message message = this.messageList.get(i);
            if (ObjUtil.isEmpty(message)) {
                continue;
            }
            if (message.getMessageType() == MessageType.ASSISTANT
                    && StrUtil.equals(lastMessage.getText(), message.getText())) {
                duplicateCount++;
            }
        }
        return duplicateCount >= this.duplicateThreshold;
    }

    private void handleStuckState() {
        String stuckPrompt = "Observed duplicate responses. Consider new strategies and avoid repeating ineffective " +
                "paths already attempted.";
        this.nextStepPrompt = stuckPrompt + (StrUtil.isNotBlank(this.nextStepPrompt) ? "\n" + this.nextStepPrompt : "");
        log.warn("Agent {} is stuck in a loop, added stuck prompt to message list", this.name);
    }
}
