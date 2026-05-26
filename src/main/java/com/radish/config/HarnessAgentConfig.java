package com.radish.config;

import io.agentscope.core.model.OpenAIChatModel;
import io.agentscope.harness.agent.HarnessAgent;
import io.agentscope.harness.agent.filesystem.spec.LocalFilesystemSpec;
import io.agentscope.harness.agent.memory.compaction.CompactionConfig;
import io.agentscope.harness.agent.memory.compaction.ToolResultEvictionConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Harness Agent 配置类
 *
 * @author luoyuheng
 * @date 2026-05-26
 */
@Slf4j
@Configuration
public class HarnessAgentConfig {

    @Value("${agentscope.model.baseUrl:https://api.deepseek.com}")
    private String modelBaseUrl;

    @Value("${agentscope.model.apiKey:}")
    private String modelApiKey;

    @Value("${agentscope.model.name:deepseek-v4-pro}")
    private String modelName;

    @Value("${agentscope.workspace:.agentscope/workspace}")
    private String workspacePath;

    /**
     * 创建并配置 HarnessAgent Bean
     */
    @Bean
    public HarnessAgent harnessAgent() throws IOException {
        log.info("初始化 HarnessAgent，modelBaseUrl: {}, modelName: {}", modelBaseUrl, modelName);

        // 初始化工作空间
        Path workspace = Paths.get(workspacePath);
        initWorkspaceIfAbsent(workspace);

        // 构建模型
        OpenAIChatModel model = OpenAIChatModel.builder()
                .baseUrl(modelBaseUrl)
                .modelName(modelName)
                .apiKey(modelApiKey)
                .build();

        // 构建 HarnessAgent
        return HarnessAgent.builder()
                .name("harness-agent")
                .description("一个乐于助人的 AI 助手")
                .sysPrompt("你是一个乐于助人的助手，可以帮助用户完成各种任务。")
                .model(model)
                .workspace(workspace)
                .enableAgentTracingLog(false)
                .compaction(CompactionConfig.builder()
                        .triggerMessages(30)
                        .keepMessages(10)
                        .flushBeforeCompact(true)
                        .offloadBeforeCompact(true)
                        .build())
                .toolResultEviction(ToolResultEvictionConfig.defaults())
                .filesystem(new LocalFilesystemSpec().executeTimeoutSeconds(120))
                .build();
    }

    /**
     * 初始化工作空间目录和 AGENTS.md 文件
     */
    private void initWorkspaceIfAbsent(Path workspace) throws IOException {
        Files.createDirectories(workspace);
        Path agentPath = workspace.resolve("AGENTS.md");
        if (Files.exists(agentPath)) {
            return;
        }
        Files.writeString(agentPath, """
                你是一个乐于助人的助手
                """);
        log.info("已初始化工作空间：{}", workspace.toAbsolutePath());
    }
}
