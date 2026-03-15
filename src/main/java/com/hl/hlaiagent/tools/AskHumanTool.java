package com.hl.hlaiagent.tools;

import cn.hutool.core.util.StrUtil;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

/**
 * 寻求人类帮助工具。
 *
 * <p>当大模型在执行任务时遇到关键信息缺失、需要用户确认，或者需要人工提供额外输入时，
 * 可以调用这个工具暂停自动推进，并把问题直接抛给当前控制台中的用户。</p>
 */
public class AskHumanTool {

    /**
     * 这里使用 Scanner 读取控制台输入，行为上对应 OpenManus 里的 input(...)
     * 但为了便于测试，额外保留了一个可传入 InputStream 的构造方法。
     */
    private final Scanner scanner;

    public AskHumanTool() {
        this(System.in);
    }

    AskHumanTool(InputStream inputStream) {
        this.scanner = new Scanner(inputStream, StandardCharsets.UTF_8);
    }

    /**
     * 向人类发起询问并等待输入。
     *
     * @param inquire 想向人类询问的问题
     * @return 人类输入的内容；如果未输入内容，则返回明确提示，方便模型决定下一步动作。
     */
    @Tool(description = "Ask human for help when more information, confirmation, or manual input is needed")
    public String askHuman(@ToolParam(description = "The question you want to ask human") String inquire) {
        // 第一步：校验问题内容，避免模型传入空字符串导致交互信息不明确。
        if (StrUtil.isBlank(inquire)) {
            return "Inquiry must not be blank.";
        }

        String normalizedInquiry = inquire.trim();

        // 第二步：把问题打印给当前控制台用户，这一步等价于 Python 版本里的 input 提示词。
        System.out.printf("Bot: %s%n%nYou: ", normalizedInquiry);

        // 第三步：等待用户输入；如果控制台当前没有可读取的输入，则给模型返回明确结果。
        if (!scanner.hasNextLine()) {
            return "No human input was provided.";
        }

        String answer = scanner.nextLine().trim();

        // 第四步：返回人类输入给模型，模型可以基于这段补充信息继续后续推理和工具调用。
        if (StrUtil.isBlank(answer)) {
            return "Human did not provide any input.";
        }
        return answer;
    }
}

