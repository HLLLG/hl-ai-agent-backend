package com.hl.hlaiagent.rag;

import com.hl.hlaiagent.exception.BusinessException;
import com.hl.hlaiagent.exception.ErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.markdown.MarkdownDocumentReader;
import org.springframework.ai.reader.markdown.config.MarkdownDocumentReaderConfig;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * LoveAppDocumentLoader 负责加载 love-app 相关的文档资源，并将其转换为 Document 对象列表。
 * 这些文档资源位于 classpath 的 document 目录下，文件格式为 Markdown (.md)。
 * 加载过程中会捕获任何异常，并将其封装为 BusinessException 抛出，以便调用方进行统一的错误处理。
 */
@Component
@Slf4j
public class LoveAppDocumentLoader {

    private final ResourcePatternResolver resourcePatternResolver;

    public LoveAppDocumentLoader(ResourcePatternResolver resourcePatternResolver) {
        this.resourcePatternResolver = resourcePatternResolver;
    }

    // Markdown 文档加载方法
    public List<Document> loadDocuments() {
        List<Document> documents = new ArrayList<>();
        try {
            // 加载 love-app 相关的文档资源
            Resource[] resources = resourcePatternResolver.getResources("classpath:document/*.md");
            for (Resource resource : resources) {
                String filename = resource.getFilename();// 获取文件名
                // 获取文件名倒数第6到倒数第4个字符作为状态（例如：_v1.md 中的 v1）
                String status = filename.substring(filename.length() - 6, filename.length() - 4);
                MarkdownDocumentReaderConfig config = MarkdownDocumentReaderConfig.builder()
                        .withHorizontalRuleCreateDocument(true)
                        .withIncludeCodeBlock(false)
                        .withIncludeBlockquote(false)
                        .withAdditionalMetadata("filename", filename)
                        .withAdditionalMetadata("status", status)
                        .build();
                MarkdownDocumentReader reader = new MarkdownDocumentReader(resource, config);
                documents.addAll(reader.get());
            }
        } catch (Exception e) {
            log.error("Markdown 文档加载失败", e);
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "Markdown 文档加载失败: " + e.getMessage());
        }
        return documents;
    }
}
