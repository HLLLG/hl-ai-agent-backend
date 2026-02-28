package com.hl.hlaiagent.advisor;

import com.hl.hlaiagent.exception.BusinessException;
import com.hl.hlaiagent.exception.ErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClientRequest;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.client.advisor.api.CallAdvisor;
import org.springframework.ai.chat.client.advisor.api.CallAdvisorChain;
import org.springframework.ai.chat.client.advisor.api.StreamAdvisor;
import org.springframework.ai.chat.client.advisor.api.StreamAdvisorChain;
import reactor.core.publisher.Flux;

import java.util.Arrays;
import java.util.List;

/**
 * 违禁词校验 Advisor
 * 在用户消息发送给 AI 之前，检查是否包含违禁词，包含则拦截并抛出异常
 */
@Slf4j
public class ProhibitedWordAdvisor implements CallAdvisor, StreamAdvisor {

    private final int order;

    /**
     * 违禁词列表
     */
    private static final List<String> PROHIBITED_WORDS = Arrays.asList(
            // 暴力相关
            "杀人", "砍人", "打人", "伤害他人", "暗杀", "投毒", "纵火", "爆炸", "绑架",
            // 色情相关
            "色情", "裸体", "性行为", "卖淫", "嫖娼",
            // 毒品相关
            "吸毒", "贩毒", "制毒", "冰毒", "海洛因", "大麻", "可卡因",
            // 赌博相关
            "赌博", "赌钱", "网赌", "赌场",
            // 欺诈相关
            "诈骗", "骗钱", "洗钱", "传销", "非法集资",
            // 自残相关
            "自杀", "自残", "割腕", "跳楼",
            // 政治敏感
            "颠覆政权", "分裂国家", "恐怖主义", "恐怖袭击",
            // 歧视相关
            "种族歧视", "性别歧视", "地域歧视",
            // 黑客/违法
            "黑客攻击", "入侵系统", "破解密码", "盗号", "木马病毒",
            // 其他
            "人肉搜索", "造谣", "诽谤"
    );

    public ProhibitedWordAdvisor() {
        this(0);
    }

    public ProhibitedWordAdvisor(int order) {
        this.order = order;
    }

    @Override
    public ChatClientResponse adviseCall(ChatClientRequest chatClientRequest, CallAdvisorChain callAdvisorChain) {
        checkProhibitedWords(chatClientRequest);
        return callAdvisorChain.nextCall(chatClientRequest);
    }

    @Override
    public Flux<ChatClientResponse> adviseStream(ChatClientRequest chatClientRequest, StreamAdvisorChain streamAdvisorChain) {
        checkProhibitedWords(chatClientRequest);
        return streamAdvisorChain.nextStream(chatClientRequest);
    }

    /**
     * 校验用户消息是否包含违禁词
     *
     * @param request 聊天请求
     */
    private void checkProhibitedWords(ChatClientRequest request) {
        String userMessage = request.prompt().getUserMessage().getText();
        if (userMessage == null || userMessage.isEmpty()) {
            return;
        }
        // 转小写统一比较
        String lowerMessage = userMessage.toLowerCase();
        for (String word : PROHIBITED_WORDS) {
            if (lowerMessage.contains(word.toLowerCase())) {
                log.warn("用户消息包含违禁词: 【{}】，消息内容: {}", word, userMessage);
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "包含违禁词【" + word + "】，请文明用语！");
            }
        }
    }

    @Override
    public String getName() {
        return this.getClass().getSimpleName();
    }

    @Override
    public int getOrder() {
        return this.order;
    }

    @Override
    public String toString() {
        return ProhibitedWordAdvisor.class.getSimpleName();
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private int order = 0;

        private Builder() {
        }

        public Builder order(int order) {
            this.order = order;
            return this;
        }

        public ProhibitedWordAdvisor build() {
            return new ProhibitedWordAdvisor(this.order);
        }
    }
}

