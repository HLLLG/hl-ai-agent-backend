package com.hl.hlaiagent.rag.documentReader;

import com.alibaba.cloud.ai.parser.tika.TikaDocumentParser;
import com.alibaba.cloud.ai.reader.github.GitHubDocumentReader;
import com.alibaba.cloud.ai.reader.github.GitHubResource;
import com.hl.hlaiagent.exception.BusinessException;
import com.hl.hlaiagent.exception.ErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.ExtractedTextFormatter;
import org.springframework.beans.factory.annotation.Value;

import java.util.ArrayList;
import java.util.List;

/**
 * GitHubDocumentReaderTemplate 是一个抽象类，定义了从 GitHub 加载文档的模板方法 loadGitHubDocumentsBatch()。
 */
@Slf4j
public abstract class GitHubDocumentReaderTemplate {

    @Value("${github.token}")
    private String githubToken;

    @Value("${github.owner}")
    private String githubOwner;

    @Value("${github.repo}")
    private String githubRepo;

    @Value("${github.branch}")
    private String githubBranch;

    @Value("${github.path}")
    private String githubPath;

    protected abstract GitHubDocumentReader createReader(TikaDocumentParser forMatter, String githubToken, String githubOwner, String githubRepo, String githubBranch, String githubPath);

    /**
     * GitHub 文档批量加载方法（支持目录，递归加载目录下所有文件）
     * @return 文档列表
     */
    public final List<Document> loadGitHubDocuments() {
        List<Document> documents = new ArrayList<>();
        try {
            // 创建自定义的文本格式化器
            ExtractedTextFormatter formatter = createFormatter();
            // 使用 tikaDocumentParser 解析文本文件
            TikaDocumentParser tikaDocumentParser = new TikaDocumentParser(formatter);
            // 创建 GitHubDocumentReader 并读取文档
            GitHubDocumentReader reader = this.createReader(tikaDocumentParser, githubToken, githubOwner, githubRepo, githubBranch, githubPath);
            documents.addAll(reader.get());
        } catch (Exception e) {
            log.error("GitHub 文档批量加载失败", e);
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "GitHub 文档批量加载失败: " + e.getMessage());
        }
        return documents;
    }

    protected ExtractedTextFormatter createFormatter() {
        return ExtractedTextFormatter.builder()
                .withNumberOfTopTextLinesToDelete(0) // 不删除文本开头的行
                .withNumberOfBottomTextLinesToDelete(0) // 不删除文本结尾的行
                .build();
    }

}
