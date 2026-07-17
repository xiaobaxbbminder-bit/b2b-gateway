package com.whatsoeversky.minder.sftp.plugins;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.whatsoeversky.minder.sftp.support.FileRunContext;
import com.whatsoeversky.minder.utils.HttpClientUtils;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.hc.client5.http.classic.methods.HttpUriRequestBase;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
public class ApiCallPlugin implements Plugin {

    @Resource
    private ObjectMapper objectMapper;

    @Override
    public String getPluginName() {
        return "api_call";
    }

    @Override
    public void execute(FileRunContext context, String args) throws IOException {
        ApiCallArg arg = objectMapper.readValue(args, ApiCallArg.class);
        if (arg.getUrl() == null || arg.getUrl().isEmpty()) {
            throw new IOException("url is required");
        }

        String body = substituteVariables(arg.getBody(), context.getFile());
        Map<String, String> headerMap = parseHeaders(arg.getHeaders());
        int connectTimeout = arg.getConnectTimeout() != null ? arg.getConnectTimeout() : 5000;
        int readTimeout = arg.getReadTimeout() != null ? arg.getReadTimeout() : 30000;

        try (CloseableHttpClient client = HttpClientUtils.createSingleUseHttpClient(connectTimeout, readTimeout)) {
            HttpUriRequestBase request = new HttpUriRequestBase(
                    arg.getMethod() != null ? arg.getMethod().toUpperCase() : "GET",
                    URI.create(arg.getUrl()));

            if (body != null && !body.isEmpty()
                    && ("POST".equalsIgnoreCase(arg.getMethod()) || "PUT".equalsIgnoreCase(arg.getMethod()) || "PATCH".equalsIgnoreCase(arg.getMethod()))) {
                request.setEntity(new StringEntity(body, ContentType.APPLICATION_JSON));
            }

            for (Map.Entry<String, String> e : headerMap.entrySet()) {
                request.setHeader(e.getKey(), e.getValue());
            }

            try (CloseableHttpResponse response = client.execute(request)) {
                int statusCode = response.getCode();
                String responseBody = "";
                try {
                    responseBody = response.getEntity() != null
                            ? EntityUtils.toString(response.getEntity()) : "";
                } catch (org.apache.hc.core5.http.ParseException e) {
                    log.warn("failed to parse response body", e);
                }

                log.info("api call done: {} {} -> {}", arg.getMethod(), arg.getUrl(), statusCode);

                Map<String, Object> res = new HashMap<>();
                res.put("statusCode", statusCode);
                res.put("body", responseBody);
                res.put("url", arg.getUrl());
                context.putContextVariables(getPluginName(),
                        FileRunContext.ContextVariable.builder().args(arg).res(res).build());
            }
        } catch (IOException e) {
            log.error("api call failed: {} {}", arg.getMethod(), arg.getUrl(), e);
            throw new IOException("api call failed: " + e.getMessage());
        }
    }

    private String substituteVariables(String template, Path file) {
        if (template == null || template.isEmpty()) return template;
        String filename = file.getFileName().toString();
        String filepath = file.toAbsolutePath().toString();
        long filesize = 0;
        try { filesize = Files.size(file); } catch (IOException ignored) {}
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        return template
                .replace("{filename}", filename)
                .replace("{filepath}", filepath)
                .replace("{filesize}", String.valueOf(filesize))
                .replace("{timestamp}", timestamp);
    }

    private Map<String, String> parseHeaders(String headersJson) {
        if (headersJson == null || headersJson.isEmpty()) return Map.of();
        try {
            return objectMapper.readValue(headersJson, new TypeReference<Map<String, String>>() {});
        } catch (Exception e) {
            log.warn("failed to parse headers", e);
            return Map.of();
        }
    }
}
