package com.hl.hlaiagent.tools;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;


class WebScrapingToolTest {


    @Test
    void scrapeWeb() {
        WebScrapingTool webScrapingTool = new WebScrapingTool();
        String url = "https://www.baidu.com";
        String result = webScrapingTool.scrapeWeb(url);
        System.out.println(result);
        assertNotNull(result);
    }
}