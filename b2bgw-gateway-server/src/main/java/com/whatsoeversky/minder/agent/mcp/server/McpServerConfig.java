package com.whatsoeversky.minder.agent.mcp.server;

import com.whatsoeversky.minder.agent.tools.SftpUserTools;
import io.modelcontextprotocol.server.McpServerFeatures;
import io.modelcontextprotocol.spec.McpSchema;
import jakarta.annotation.Resource;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class McpServerConfig {
    @Resource
    private SftpUserTools sftpUserTools;

    @Bean
    public List<McpServerFeatures.SyncPromptSpecification> myPrompts() {
        var prompt = McpSchema.Prompt.builder("getPasswordByUsername")
                .arguments(List.of(McpSchema.PromptArgument.builder("username")
                        .title("用户名")
                        .description("需要查询的用户名")
                        .required(true).build())
                ).description("根据某个用户名查找配置的账号密码(加密后的密码)").build();


        var promptSpecification = new McpServerFeatures.SyncPromptSpecification(prompt, (exchange, getPromptRequest) -> {
            String nameArgument = (String) getPromptRequest.arguments().get("username");
            if (nameArgument == null) {
                nameArgument = "friend";
            }
            var userMessage = new McpSchema.PromptMessage(McpSchema.Role.USER, McpSchema.TextContent.builder("我需要查询 " + nameArgument + "的密码是什么。").build());
            return McpSchema.GetPromptResult.builder(List.of(userMessage)).description("一个查询账号对应密码的消息").build();
        });

        return List.of(promptSpecification);
    }

    @Bean
    public ToolCallbackProvider toolCallbackProvider() {
        return MethodToolCallbackProvider.builder().toolObjects(sftpUserTools).build();
    }
}
