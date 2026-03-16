package com.hl.hlaiagent.tools;

import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 联网搜索工具
 */
@Slf4j
public class WebSearchTool {

    // 搜索API的URL
    private static final String SEARCH_API_URL = "https://www.searchapi.io/api/v1/search";

    private final String API_KEY;

    public WebSearchTool(String API_KEY) {
        this.API_KEY = API_KEY;
    }

    @Tool(description = "Perform a web searchWeb using Baidu and return the top 5 results")
    public String searchWeb(@ToolParam(description = "Search query keyword") String query) {
        // 1 构造搜索请求
        Map<String, Object> params = new HashMap<>();
        params.put("q", query);
        params.put("api_key", API_KEY);
        params.put("engine", "baidu"); // 使用百度搜索引擎
        try {
            // 2 发送搜索请求并获取结果
            String response = HttpUtil.get(SEARCH_API_URL, params);
            // 3 解析搜索结果
            JSONObject jsonObject = JSONUtil.parseObj(response);
            JSONArray organicResults = jsonObject.getJSONArray("organic_results");
            if (organicResults == null || organicResults.isEmpty()) {
                return "No search results found.";
            }
            // 只取前5条结果
            int endIndex = Math.min(5, organicResults.size());
            List<Object> objects = organicResults.subList(0, endIndex);
            // 4 格式化搜索结果
            StringBuilder resultBuilder = new StringBuilder();
            for (Object object : objects) {
                JSONObject result = (JSONObject) object;
                String title = result.getStr("title");
                String link = result.getStr("link");
                String snippet = result.getStr("snippet");
                resultBuilder.append(String.format("标题: %s\n链接: %s\n摘要: %s\n\n", title, link, snippet));
            }
            return resultBuilder.toString();
        } catch (Exception e) {
            log.error("Error searching Baidu", e);
            return "Error searching Baidu: " + e.getMessage();
        }

    }
}
