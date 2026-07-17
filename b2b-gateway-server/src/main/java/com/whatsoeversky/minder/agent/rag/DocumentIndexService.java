package com.whatsoeversky.minder.agent.rag;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

@Slf4j
@Service
public class DocumentIndexService {

    @Value("${sftp.docs-path}")
    private String docsPath;

    @Autowired
    private QdrantClient qdrantClient;

    @PostConstruct
    public void init() {
        try {
            var results = qdrantClient.search("init", 1);
            if (results.isEmpty()) {
                log.info("向量库为空，开始初始化文档索引...");
                rebuildIndex();
            } else {
                log.info("向量库已有数据，跳过初始化");
            }
        } catch (Exception e) {
            log.warn("向量库查询异常，尝试重建索引", e);
            rebuildIndex();
        }
    }

    public void rebuildIndex() {
        Path dir = Paths.get(docsPath);
        if (!Files.isDirectory(dir)) {
            log.warn("文档目录不存在: {}", dir);
            return;
        }

        try {
            qdrantClient.clearCollection();
            List<QdrantClient.QdrantPoint> chunks = new ArrayList<>();

            try (Stream<Path> files = Files.walk(dir, 1)) {
                files.filter(f -> f.toString().endsWith(".md"))
                        .sorted()
                        .forEach(file -> {
                            try {
                                String filename = file.getFileName().toString();
                                log.info("索引文档: {}", filename);
                                String content = Files.readString(file, StandardCharsets.UTF_8);
                                chunks.addAll(chunkDocument(filename, content));
                            } catch (IOException e) {
                                log.warn("读取文档失败: {}", file, e);
                            }
                        });
            }

            qdrantClient.upsertPoints(chunks);
            log.info("索引完成，共 {} 个片段", chunks.size());
        } catch (Exception e) {
            log.error("重建索引失败", e);
            throw new RuntimeException("索引重建失败", e);
        }
    }

    private List<QdrantClient.QdrantPoint> chunkDocument(String filename, String content) {
        List<QdrantClient.QdrantPoint> chunks = new ArrayList<>();
        String[] lines = content.split("\n");
        StringBuilder currentContent = new StringBuilder();
        String currentHeading = "";
        String prevContent = "";

        for (String line : lines) {
            if (line.startsWith("## ")) {
                if (!currentContent.isEmpty()) {
                    String chunkText = currentContent.toString().trim();
                    if (!chunkText.isEmpty()) {
                        chunks.add(new QdrantClient.QdrantPoint(chunkText, filename, currentHeading));
                    }
                }
                currentHeading = line.replace("## ", "").trim();
                currentContent = new StringBuilder();
                currentContent.append(prevContent).append("\n");
                currentContent.append(line).append("\n");
                prevContent = "";
            } else {
                currentContent.append(line).append("\n");
                String trimmed = line.trim();
                if (!trimmed.isEmpty()) {
                    prevContent = trimmed;
                }
            }
        }
        if (!currentContent.isEmpty()) {
            String chunkText = currentContent.toString().trim();
            if (!chunkText.isEmpty()) {
                chunks.add(new QdrantClient.QdrantPoint(chunkText, filename, currentHeading));
            }
        }
        return chunks;
    }
}
