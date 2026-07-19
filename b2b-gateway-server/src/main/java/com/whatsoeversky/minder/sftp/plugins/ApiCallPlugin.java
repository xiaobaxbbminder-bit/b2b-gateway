package com.whatsoeversky.minder.sftp.plugins;

import com.alibaba.fastjson.JSON;
import com.whatsoeversky.minder.sftp.support.FileRunContext;
import com.whatsoeversky.minder.utils.HttpClientUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.hc.client5.http.classic.methods.HttpUriRequestBase;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.HttpHeaders;
import org.apache.hc.core5.http.io.entity.ByteArrayEntity;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@Component
public class ApiCallPlugin implements Plugin {
    @Override
    public String getPluginName() {
        return "api_call";
    }

    @Override
    public void execute(FileRunContext context, String args) throws IOException {
        ApiCallArg arg = JSON.parseObject(args, ApiCallArg.class);
        FileRunContext.ExpressionParser expressionParser = context.getExpressionParser();
        String body = expressionParser.parseExpression(arg.getBody());
        Map<String, String> headerMap = arg.getHeaders();
        int connectTimeout = arg.getConnectTimeout();
        int readTimeout = arg.getReadTimeout();
        HttpUriRequestBase request = new HttpUriRequestBase(arg.getMethod().toUpperCase(), URI.create(arg.getUrl()));
        // 默认设置为json
        String contentType = ContentType.APPLICATION_JSON.getMimeType();
        for (Map.Entry<String, String> e : headerMap.entrySet()) {
            String value = expressionParser.parseExpression(e.getValue());
            if (e.getKey().equalsIgnoreCase(HttpHeaders.CONTENT_TYPE)) {
                contentType = value;
            } else {
                request.setHeader(e.getKey(), value);
            }
        }
        request.setHeader(HttpHeaders.CONTENT_TYPE, contentType);
        if (StringUtils.hasLength(body)
                && (HttpMethod.POST.name().equalsIgnoreCase(arg.getMethod())
                || HttpMethod.PUT.name().equalsIgnoreCase(arg.getMethod())
                || HttpMethod.PATCH.name().equalsIgnoreCase(arg.getMethod()))) {
            request.setEntity(new ByteArrayEntity(body.getBytes(StandardCharsets.UTF_8), ContentType.create(contentType)));
        }
        try (CloseableHttpClient client = HttpClientUtils.createSingleUseHttpClient(connectTimeout, readTimeout)) {
            AtomicInteger statusRes = new AtomicInteger();
            String response = client.execute(request, classicHttpResponse -> {
                int statusCode = classicHttpResponse.getCode();
                statusRes.set(statusCode);
                log.info("api call done: {} {} -> {}", arg.getMethod(), arg.getUrl(), statusCode);
                return EntityUtils.toString(classicHttpResponse.getEntity());
            });
            Map<String, Object> res = new HashMap<>();
            res.put("statusCode", statusRes.get());
            res.put("body", response);
            res.put("url", arg.getUrl());
            context.putContextVariables(getPluginName(), FileRunContext.ContextVariable.builder().args(arg).res(res).build());
        }
    }
}
