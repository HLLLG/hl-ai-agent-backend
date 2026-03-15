package com.hl.hlaiagent.tools;

import org.springframework.ai.tool.annotation.Tool;

/**
 * 终止工具类，定义了一个用于终止代理执行的工具。该工具可以被代理调用，以便在特定条件下停止代理的执行，避免不必要的资源消耗和错误发生。
 */
public class TerminateTool {

    @Tool(description = """  
            Terminate the interaction when the request is met OR if the assistant cannot proceed further with the task.  
            "When you have finished all the tasks, call this tool to end the work.  
            """)
    public String doTerminate() {
        return "Terminating the interaction as requested.";
    }

}
