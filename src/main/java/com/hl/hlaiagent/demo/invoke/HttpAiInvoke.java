package com.hl.hlaiagent.demo.invoke;

import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * Http 方式调用ai
 */
public class HttpAiInvoke {
    public static void main(String[] args) {
        // 请将下行中的apiKey替换为您的百炼API Key
        String apiKey = TestApiKey.API_KEY;
        String url = "https://dashscope.aliyuncs.com/api/v1/services/aigc/text-generation/generation";

        // 设置请求头
        Map<String, String> headers = new HashMap<>();
        headers.put("Authorization", "Bearer " + apiKey);
        headers.put("Content-Type", "application/json");

        // 构建请求体
        JSONObject requestBody = new JSONObject();
        requestBody.set("model", "qwen-plus"); // 模型名称（必须是字符串）

        // messages 必须是 JSON 数组，不能是 Java 的 JSONObject[]
        JSONArray messages = new JSONArray();

        // 添加系统消息
        JSONObject systemMessage = new JSONObject();
        systemMessage.set("role", "system");
        systemMessage.set("content", "You are a helpful assistant.");
        messages.add(systemMessage);

        // 添加用户消息
        JSONObject userMessage = new JSONObject();
        userMessage.set("role", "user");
        userMessage.set("content", "你是谁？");
        messages.add(userMessage);

        JSONObject input = new JSONObject();
        input.set("messages", messages);
        requestBody.set("input", input);

        // 设置参数
        JSONObject parameters = new JSONObject();
        parameters.set("result_format", "message");
        requestBody.set("parameters", parameters);

        // 发送HTTP POST请求
        try (HttpResponse response = HttpRequest.post(url)
                .addHeaders(headers)
                .body(requestBody.toString())
                .execute()) {

            // 输出响应结果
            if (response.isOk()) {
                System.out.println("请求成功!");
                System.out.println("Response: " + response.body());
            } else {
                System.out.println("Request failed with status: " + response.getStatus());
                System.out.println("Response: " + response.body());
            }
        }
    }
}
