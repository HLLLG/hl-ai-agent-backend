package com.hl.hlaiagent.tools;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpUtil;
import com.hl.hlaiagent.constant.FileConstant;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.lang.Nullable;

import java.io.File;

/**
 * 资源下载工具
 */
@Slf4j
public class ResourceDownloadTool {

    private static final String DOWNLOAD_DIR = FileConstant.FILE_SAVE_PATH + "/downloads";

    /**
     * 下载资源
     *
     * @param url 资源URL
     * @return 下载后的本地路径
     */
    @Tool(description =
            "Download a resource from a given URL. Optionally specify a file name. If not provided, the " + "file " +
                    "name will be derived from the URL.")
    public String downloadResource(@ToolParam(description = "URL of the resource to download") String url,
                                   @ToolParam(description = "Name of the file to save the downloaden resource") @Nullable String fileName) {
        // 使用Hutool的HttpUtil下载资源
        try {
            if (StrUtil.isBlank(fileName)) {
                fileName = url.substring(url.lastIndexOf("/") + 1);
            }
            // 创建目录
            FileUtil.mkdir(DOWNLOAD_DIR);
            String filePath = DOWNLOAD_DIR + "/" + fileName;
            HttpUtil.downloadFile(url, new File(filePath));
            return "Resource downloaded successfully: " + filePath;
        } catch (Exception e) {
            log.error("Failed to download resource from URL: " + url, e);
            return "Failed to download resource: " + e.getMessage();
        }
    }
}
