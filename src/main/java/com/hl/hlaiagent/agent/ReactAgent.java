package com.hl.hlaiagent.agent;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;

/**
 * 反应式代理类，继承自基础代理类，定义了反应式代理的特定行为和方法。反应式代理在执行任务时会根据当前的输入和环境进行思考和行动，以实现更智能和灵活的行为。
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Slf4j
public abstract class ReactAgent extends BaseAgent{

    /**
     * 思考方法，定义了代理在执行任务时的思考过程和逻辑
     * @return 是否需要继续执行下一步操作，true表示继续执行，false表示停止执行
     */
    public abstract boolean think();

    /**
     * 行动方法，定义了代理在执行任务时的具体行动和操作
     * @return
     */
    public abstract String act();

    /**
     * 单步执行方法，定义了代理在每一步执行时的具体操作和逻辑。
     * 首先调用思考方法，如果思考结果表明需要继续执行下一步操作，则调用行动方法，否则返回思考完成的提示信息。
     * @return
     */
    @Override
    public String step() {
        try {
            boolean shouldAct = think();
            if (!shouldAct) {
                return "Thinking complete - no action needed";
            }
            return act();
        } catch (Exception e) {
            log.error("Error during step execution: {}", e.getMessage(), e);
            return "do step error: " + e.getMessage();
        }
    }

}
