package com.hl.hlaiagent.tools;

import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;

/**
 * 网页抓取工具类，提供从指定URL抓取网页内容的功能。
 */
@Slf4j
public class WebScrapingTool {

    /**
     * 从指定URL抓取网页内容。
     *
     * @param url 要抓取的网页URL
     * @return 抓取到的网页内容
     */
    @Tool(description = "scrape content from a web page")
    public String scrapeWeb(@ToolParam(description = "url of the scraped web") String url) {
         try {
             Document doc = Jsoup.connect(url).get();
             return doc.html();
         } catch (Exception e) {
             log.error("Error scraping web page: {}", e.getMessage());
             return "Error scraping web page: " + e.getMessage();
         }}
}
