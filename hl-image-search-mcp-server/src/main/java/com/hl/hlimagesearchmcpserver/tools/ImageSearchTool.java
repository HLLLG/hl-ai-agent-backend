package com.hl.hlimagesearchmcpserver.tools;

import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class ImageSearchTool {

    // Pexels api key
    private final String API_KEY = "eCo6FEz0HX6tYBVkNhlsa6U9X4HsNw5Kgv109zvJIF9L968hDzxoBadE";

    // Pexels 常规搜索接口
    private final String API_URL = "https://api.pexels.com/v1/search";

    @Tool(name = "search_photos", description = "Search photos from Pexels by query")
    public String searchPhotos(@ToolParam(required = true, description = "The search query, for example" +
            " nature, ocean, tigers or group " + "of people working.") String query) {

        try {
            return String.join(",", searchMediumImages(query));
        } catch (Exception e) {
            return "Error search photos from Pexels by query: " + e.getMessage();
        }
    }

    public List<String> searchMediumImages(String query) {
        // 设置请求头
        Map<String, String> headers = new HashMap<>();
        headers.put("Authorization", API_KEY);

        // 设置请求参数
        Map<String, Object> params = new HashMap<>();
        params.put("query", query);

        // 发送 GET 请求
        String response = HttpUtil.createGet(API_URL)
                .addHeaders(headers)
                .form(params)
                .execute()
                .body();

        // 解析响应JSON
        List<String> collect = JSONUtil.parseObj(response)
                .getJSONArray("photos")
                .stream()
                .map(photo -> (JSONObject) photo)
                .map(photo -> photo.getJSONObject("src").getStr("medium"))
                .filter(StrUtil::isNotBlank)
                .collect(Collectors.toList());
        return collect;


    }
}
