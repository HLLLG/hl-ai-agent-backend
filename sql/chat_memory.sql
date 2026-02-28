create database hl_ai_agent;

use hl_ai_agent;

-- Spring AI JDBC 聊天记忆表（框架自动创建，设置 initialize-schema: always 后无需手动执行）
-- 如需手动创建，可执行以下 SQL：
CREATE TABLE IF NOT EXISTS SPRING_AI_CHAT_MEMORY
(
    `conversation_id` VARCHAR(36)                                    NOT NULL COMMENT '会话ID',
    `content`         TEXT                                           NOT NULL COMMENT '消息内容',
    `type`            ENUM ('USER', 'ASSISTANT', 'SYSTEM', 'TOOL')  NOT NULL COMMENT '消息类型',
    `timestamp`       TIMESTAMP                                     NOT NULL COMMENT '创建时间',
    INDEX `SPRING_AI_CHAT_MEMORY_CONVERSATION_ID_TIMESTAMP_IDX` (`conversation_id`, `timestamp`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci
  COMMENT = 'Spring AI 聊天记忆表';

