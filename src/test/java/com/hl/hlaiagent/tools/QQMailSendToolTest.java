package com.hl.hlaiagent.tools;

import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mail.javamail.JavaMailSender;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
class QQMailSendToolTest {

    @Resource
    JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String mailUsername;

    @Test
    void sendMail() {
        QQMailSendTool tool = new QQMailSendTool(mailSender, mailUsername);

        String result = tool.sendMail(mailUsername, "测试主题", "测试内容");
        assertEquals("Mail sent successfully to: " + mailUsername, result);
    }
}

