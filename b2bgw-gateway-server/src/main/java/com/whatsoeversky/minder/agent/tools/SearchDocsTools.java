package com.whatsoeversky.minder.agent.tools;

import com.whatsoeversky.minder.agent.rag.QdrantClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Component
public class SearchDocsTools {

    @Autowired
    private QdrantClient qdrantClient;

    @Tool(description = "搜索系统培训文档。当用户询问系统功能的配置方法、使用步骤、业务场景说明时，调用此工具查询相关文档内容")
    public String searchDocs(
            @ToolParam(description = "搜索关键词，例如：创建服务端账号、配置密钥、文件系统类型、用户类型说明") String query) {
        log.info("LLM 请求检索文档: {}", query);
        try {
            List<Map<String, String>> results = qdrantClient.search(query, 8);
            if (results.isEmpty()) {
                log.info("检索结果: 无匹配");
                return "未找到相关文档内容";
            }
            if (log.isInfoEnabled()) {
                for (int i = 0; i < results.size(); i++) {
                    Map<String, String> item = results.get(i);
                    log.info("检索结果[{}]: source={}, heading={}, content_len={}",
                            i, item.get("source"), item.get("heading"),
                            item.getOrDefault("content", "").length());
                }
            }
            return results.stream()
                    .map(item -> {
                        String source = item.getOrDefault("source", "");
                        String heading = item.getOrDefault("heading", "");
                        String content = item.getOrDefault("content", "");
                        String header = (source + " - " + heading).replaceAll("^ - ", "").replaceAll(" - $", "");
                        return "**来源：" + header + "**\n\n" + content;
                    })
                    .collect(Collectors.joining("\n\n---\n\n"));
        } catch (Exception e) {
            return "文档检索失败：" + e.getMessage();
        }
    }
}
