package com.hl.hlaiagent.rag.documentReader;

import com.alibaba.cloud.ai.parser.tika.TikaDocumentParser;
import com.alibaba.cloud.ai.reader.github.GitHubDocumentReader;
import com.alibaba.cloud.ai.reader.github.GitHubResource;
import org.springframework.stereotype.Service;

/**
 * GitHubSingleDocumentReader 是 GitHubDocumentReaderTemplate 的具体实现类，负责加载 GitHub 上的单个文件。
 */
@Service
public class GitHubSingleDocumentReader extends GitHubDocumentReaderTemplate {

    @Override
    protected GitHubDocumentReader createReader(TikaDocumentParser forMatter, String githubToken, String githubOwner, String githubRepo, String githubBranch, String githubPath) {
        // 使用 Builder 模式批量加载目录下的所有文件
        GitHubResource gitHubResources = GitHubResource.builder()
                .gitHubToken(githubToken)
                .owner(githubOwner)
                .repo(githubRepo)
                .branch(githubBranch)
                .path(githubPath)
                .build();
        // 创建 GitHubDocumentReader
        return  new GitHubDocumentReader(gitHubResources, forMatter);
    }
}
