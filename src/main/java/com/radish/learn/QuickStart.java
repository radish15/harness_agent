package com.radish.learn;

import io.agentscope.core.agent.RuntimeContext;
import io.agentscope.core.message.Msg;
import io.agentscope.core.model.DashScopeChatModel;
import io.agentscope.core.model.OpenAIChatModel;
import io.agentscope.harness.agent.HarnessAgent;
import io.agentscope.harness.agent.filesystem.spec.LocalFilesystemSpec;
import io.agentscope.harness.agent.memory.compaction.CompactionConfig;
import io.agentscope.harness.agent.memory.compaction.ToolResultEvictionConfig;
import io.github.cdimascio.dotenv.Dotenv;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * @author luoyuheng
 * @Description
 * @date 2026-05-25 22:03
 */
public class QuickStart {
	public static void main(String[] args) throws IOException {
		// 加载环境变量
		Dotenv.configure().ignoreIfMissing().systemProperties().load();

		Path workspace = Paths.get(".agentscope/workspace");

		initWorkspaceIfAbsent(workspace);

		String deepseekBaseUrl = "https://api.deepseek.com";
		String deepseekApiKey = "";
		String modelName = "deepseek-v4-pro";
		OpenAIChatModel model = OpenAIChatModel.builder()
				.baseUrl(deepseekBaseUrl)
				.modelName(modelName)
				.apiKey(deepseekApiKey)
				.build();

		HarnessAgent agent = HarnessAgent.builder()
				.name("quickstart-agent")
				.model(model)
				.workspace(workspace)
				.enableAgentTracingLog(false)
				.compaction(CompactionConfig.builder() // 压缩
						.triggerMessages(30) // 30条触发压缩
						.keepMessages(10)  // 保持10条不压缩
						.flushBeforeCompact(true)  // 压缩前把内存中的内容刷新到磁盘中
						.offloadBeforeCompact(true)  // 压缩前把内存中的内容刷新到磁盘中后，内存释放
						.build())
				.toolResultEviction(ToolResultEvictionConfig.defaults())
				.filesystem(new LocalFilesystemSpec().executeTimeoutSeconds(120))  // 文件系统(默认值)
				.build();

		RuntimeContext ctx = RuntimeContext.builder()
				.userId("radish-1")
				.sessionId("session-1")
				.build();

		Msg msg = agent.call(Msg.builder().textContent("我是大帅哥，我喜欢打篮球").build(), ctx).block();
		System.out.println("Assistant: " + msg.getTextContent());

		Msg msg1 = agent.call(Msg.builder().textContent("你知道我的爱好吗").build(), ctx).block();
		System.out.println("Assistant1: " + msg1.getTextContent());
	}

	private static void initWorkspaceIfAbsent(Path workspace) throws IOException {
		Files.createDirectories(workspace);
		Path agentPath = workspace.resolve("AGENTS.md");
		if (Files.exists(agentPath)) {
			return;
		}
		Files.writeString(agentPath, """
				你是一个乐于助人的助手	
				""");

	}
}
