package com.hl.hlaiagent.tools;

import cn.hutool.core.util.StrUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import java.util.regex.Pattern;

/**
 * QQ 邮箱发送工具类，提供发送纯文本邮件的功能。
 */
@Slf4j
public class QQMailSendTool {

    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");

    private final JavaMailSender mailSender;
    private final String fromAddress;

    public QQMailSendTool(JavaMailSender mailSender, String fromAddress) {
        this.mailSender = mailSender;
        this.fromAddress = fromAddress;
    }

    /**
     * 发送 QQ 邮箱纯文本邮件。
     *
     * @param to      收件人邮箱
     * @param subject 邮件主题
     * @param content 邮件正文
     * @return 发送结果
     */
    @Tool(description = "Send a QQ email")
    public String sendMail(@ToolParam(description = "Recipient email address") String to,
                           @ToolParam(description = "Mail subject") String subject,
                           @ToolParam(description = "Mail content") String content) {
        // 第一步：校验必要参数，避免出现空收件人、空主题、空正文。
        if (StrUtil.isBlank(to)) {
            return "Recipient email address must not be blank.";
        }
        if (StrUtil.isBlank(subject)) {
            return "Mail subject must not be blank.";
        }
        if (StrUtil.isBlank(content)) {
            return "Mail content must not be blank.";
        }
        if (StrUtil.isBlank(fromAddress)) {
            return "Mail sender address is not configured. Please set spring.mail.username.";
        }

        String normalizedTo = to.trim();
        String normalizedSubject = subject.trim();
        String normalizedContent = content.trim();

        // 第二步：校验发件人与收件人邮箱格式，尽早给出明确提示。
        if (!isValidEmail(fromAddress)) {
            return "Configured sender email address is invalid: " + fromAddress;
        }
        if (!isValidEmail(normalizedTo)) {
            return "Recipient email address is invalid: " + normalizedTo;
        }

        try {
            // 第三步：构造纯文本邮件对象，设置发件人、收件人、主题和正文。
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromAddress);
            message.setTo(normalizedTo);
            message.setSubject(normalizedSubject);
            message.setText(normalizedContent);

            // 第四步：通过 Spring Boot 提供的 JavaMailSender 发送邮件。
            // 注意：QQ 邮箱这里通常需要配置 SMTP 授权码，而不是 QQ 登录密码。
            mailSender.send(message);
            return "Mail sent successfully to: " + normalizedTo;
        } catch (MailException e) {
            log.error("Failed to send mail to: {}", normalizedTo, e);
            return "Failed to send mail: " + e.getMessage();
        } catch (Exception e) {
            log.error("Unexpected error while sending mail to: {}", normalizedTo, e);
            return "Unexpected error while sending mail: " + e.getMessage();
        }
    }

    private boolean isValidEmail(String email) {
        return StrUtil.isNotBlank(email) && EMAIL_PATTERN.matcher(email.trim()).matches();
    }
}
