package com.hl.hlaiagent.tools;


import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.IORuntimeException;
import com.hl.hlaiagent.constant.FileConstant;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;

/**
 * 文件读写工具类，提供文件内容读取功能。
 */
@Slf4j
public class FileOperationTool {

    private final String FILE_DIR = FileConstant.FILE_SAVE_PATH + "/files";

    /**
     * Reads the content of a file.
     *
     * @param fileName The name of the file to read.
     * @return The content of the file or an error message if reading fails.
     */
    @Tool(description = "Read the content of a file")
    public String readFile(@ToolParam(description = "Name of the file to read") String fileName) {

        String filePath = FILE_DIR + "/" + fileName;
        try {
            return FileUtil.readUtf8String(filePath);
        } catch (IORuntimeException e) {
            log.error("Failed to read file: " + fileName, e);
            return "Failed to read file: " + e.getMessage();
        }
    }

    /**
     * Writes content to a file.
     * @param fileName The name of the file to write to.
     * @param content The content to write to the file.
     * @return A success message or an error message if writing fails.
     */
    @Tool(description = "Write content to a file")
    public String writeFile(@ToolParam(description = "Name of the file to write to") String fileName,
                            @ToolParam(description = "Content to write to the file") String content) {
        String filePath = FILE_DIR + "/" + fileName;
        try {
            FileUtil.writeUtf8String(content, filePath);
            return "File written successfully: " + fileName;
        } catch (IORuntimeException e) {
            log.error("Failed to write file: " + fileName, e);
            return "Failed to write file: " + e.getMessage();
        }
    }
}
