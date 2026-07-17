package com.whatsoeversky.minder.agent.rag;

import jakarta.annotation.PostConstruct;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Slf4j
@Service
public class QdrantClient {

    @Value("${spring.ai.vectorstore.qdrant.host:localhost}")
    private String host;

    @Value("${spring.ai.vectorstore.qdrant.port:6333}")
    private int port;

    @Value("${spring.ai.vectorstore.qdrant.collection-name:sftp_rpa_docs}")
    private String collectionName;

    @Autowired
    private EmbeddingModel embeddingModel;

    private final RestTemplate rest = new RestTemplate();

    private int pointIdCounter = 1;

    private String baseUrl() {
        return "http://" + host + ":" + port;
    }

    @PostConstruct
    public void init() {
        try {
            rest.getForEntity(baseUrl() + "/collections/" + collectionName, String.class);
            log.info("Qdrant collection '{}' 已存在", collectionName);
        } catch (Exception e) {
            log.warn("Qdrant collection '{}' 不存在，尝试创建", collectionName);
            createCollection();
        }
    }

    private void createCollection() {
        try {
            Map<String, Object> body = new HashMap<>();
            Map<String, Object> vectors = new HashMap<>();
            vectors.put("size", 1024);
            vectors.put("distance", "Cosine");
            body.put("vectors", vectors);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            rest.put(baseUrl() + "/collections/" + collectionName,
                    new HttpEntity<>(body, headers), String.class);
            log.info("Qdrant collection '{}' 创建成功", collectionName);
        } catch (Exception e) {
            log.error("创建 Qdrant collection 失败", e);
            throw new RuntimeException("Qdrant 初始化失败", e);
        }
    }

    public void upsertPoints(List<QdrantPoint> points) {
        if (points.isEmpty()) return;

        List<Map<String, Object>> pointList = new ArrayList<>();
        for (QdrantPoint p : points) {
            List<Double> vector = toList(embeddingModel.embed(p.getContent()));
            Map<String, Object> pt = new HashMap<>();
            pt.put("id", pointIdCounter++);
            pt.put("vector", vector);
            Map<String, Object> payload = new HashMap<>();
            payload.put("content", p.getContent());
            payload.put("source", p.getSource());
            payload.put("heading", p.getHeading());
            pt.put("payload", payload);
            pointList.add(pt);
        }

        Map<String, Object> body = new HashMap<>();
        body.put("points", pointList);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        rest.put(baseUrl() + "/collections/" + collectionName + "/points",
                new HttpEntity<>(body, headers), String.class);
    }

    public List<Map<String, String>> search(String query, int limit) {
        List<Double> queryVector = toList(embeddingModel.embed(query));
        Map<String, Object> body = new HashMap<>();
        body.put("vector", queryVector);
        body.put("limit", limit);
        body.put("with_payload", true);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        Map<String, Object> resp = rest.postForObject(
                baseUrl() + "/collections/" + collectionName + "/points/search",
                new HttpEntity<>(body, headers),
                Map.class);

        List<Map<String, String>> results = new ArrayList<>();
        if (resp == null || !resp.containsKey("result")) return results;

        List<Map<String, Object>> resultList = (List<Map<String, Object>>) resp.get("result");
        for (Map<String, Object> r : resultList) {
            Map<String, Object> payload = (Map<String, Object>) r.get("payload");
            if (payload != null) {
                Map<String, String> item = new HashMap<>();
                item.put("content", (String) payload.getOrDefault("content", ""));
                item.put("source", (String) payload.getOrDefault("source", ""));
                item.put("heading", (String) payload.getOrDefault("heading", ""));
                results.add(item);
            }
        }
        return results;
    }

    public void clearCollection() {
        try {
            Map<String, Object> body = new HashMap<>();
            body.put("filter", new HashMap<>());
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            rest.postForEntity(baseUrl() + "/collections/" + collectionName + "/points/delete",
                    new HttpEntity<>(body, headers), String.class);
            pointIdCounter = 1;
            log.info("Qdrant collection '{}' 已清空", collectionName);
        } catch (Exception e) {
            log.warn("清空 collection 失败", e);
        }
    }

    private List<Double> toList(Object embedResult) {
        if (embedResult instanceof float[]) {
            float[] arr = (float[]) embedResult;
            List<Double> list = new ArrayList<>(arr.length);
            for (float v : arr) list.add((double) v);
            return list;
        }
        if (embedResult instanceof List) {
            return (List<Double>) embedResult;
        }
        return Collections.nCopies(1024, 0.0);
    }

    @Getter
    @AllArgsConstructor
    public static class QdrantPoint {
        private String content;
        private String source;
        private String heading;
    }
}
