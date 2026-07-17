package com.whatsoeversky.minder.utils;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.config.ConnectionConfig;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.config.TlsConfig;
import org.apache.hc.client5.http.impl.DefaultHttpRequestRetryStrategy;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManager;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManagerBuilder;
import org.apache.hc.core5.http.io.SocketConfig;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.http.ssl.TLS;
import org.apache.hc.core5.util.Timeout;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDateTime;

@Component
@Slf4j
public class HttpClientUtils {

    public static PoolingHttpClientConnectionManager createPoolingHttpClientConnectionManager(int maxTotal,
            int maxPerRoute,
            int connectTimeoutSeconds,
            int socketTimeoutSeconds,
            int soTimeoutSeconds) {
        // PoolingHttpClientConnectionManager poolingHttpClientConnectionManager =
        // PoolingHttpClientConnectionManagerBuilder.create()
        // .setConnectionTimeToLive(null)
        // .build();

        PoolingHttpClientConnectionManager poolingHttpClientConnectionManager = new PoolingHttpClientConnectionManager();
        // 设置连接总数
        poolingHttpClientConnectionManager.setMaxTotal(maxTotal);
        // 设置每个路由的连接数量
        poolingHttpClientConnectionManager.setDefaultMaxPerRoute(maxPerRoute);
        poolingHttpClientConnectionManager.setDefaultTlsConfig(TlsConfig.custom()
                .setSupportedCipherSuites(
                        "TLS_AES_256_GCM_SHA384",
                        "TLS_CHACHA20_POLY1305_SHA256",
                        "TLS_AES_128_GCM_SHA256",
                        "TLS_ECDHE_RSA_WITH_AES_256_GCM_SHA384",
                        "TLS_ECDHE_RSA_WITH_AES_128_GCM_SHA256")
                // 设置TLS版本
                .setSupportedProtocols(TLS.V_1_2, TLS.V_1_3)
                .build());
        poolingHttpClientConnectionManager.setDefaultSocketConfig(SocketConfig.custom()
                // 读取无响应超时时间
                .setSoTimeout(Timeout.ofSeconds(soTimeoutSeconds))
                .build());
        poolingHttpClientConnectionManager.setDefaultConnectionConfig(ConnectionConfig.custom()
                .setConnectTimeout(Timeout.ofSeconds(connectTimeoutSeconds))
                .setSocketTimeout(Timeout.ofSeconds(socketTimeoutSeconds))
                .build());
        return poolingHttpClientConnectionManager;

    }

    /**
     * 从连接池里面获取一个http连接，这种不需要主动close
     *
     * @param httpClientConnectionManager httpClientConnectionManager
     * @return CloseableHttpClient
     */
    public static CloseableHttpClient getPoolingHttpClient(
            PoolingHttpClientConnectionManager httpClientConnectionManager,
            int connectionRequestTimeoutSeconds,
            int responseTimeoutSeconds) {
        return HttpClients.custom()
                .setConnectionManager(httpClientConnectionManager)
                .setDefaultRequestConfig(RequestConfig.custom()
                        .setConnectionRequestTimeout(Timeout.ofSeconds(connectionRequestTimeoutSeconds))
                        .setResponseTimeout(Timeout.ofSeconds(responseTimeoutSeconds))
                        .build())
                .setRetryStrategy(new DefaultHttpRequestRetryStrategy(2, Timeout.ofSeconds(5)))
                .setUserAgent("sftp-rpa-engine")
                .setConnectionManagerShared(true)
                .build();
    }

    /**
     * 创建一个单次使用的 HTTP 客户端，每次请求新建连接，完成后需 close 释放
     */
    public static CloseableHttpClient createSingleUseHttpClient(int connectTimeoutMs, int readTimeoutMs) {
        PoolingHttpClientConnectionManager connMgr = new PoolingHttpClientConnectionManager();
        connMgr.setDefaultConnectionConfig(ConnectionConfig.custom()
                .setConnectTimeout(Timeout.ofMilliseconds(connectTimeoutMs))
                .build());
        connMgr.setDefaultSocketConfig(SocketConfig.custom()
                .setSoTimeout(Timeout.ofMilliseconds(readTimeoutMs))
                .build());
        return HttpClients.custom()
                .setConnectionManager(connMgr)
                .setDefaultRequestConfig(RequestConfig.custom()
                        .setConnectionRequestTimeout(Timeout.ofMilliseconds(connectTimeoutMs))
                        .setResponseTimeout(Timeout.ofMilliseconds(readTimeoutMs))
                        .build())
                .disableAutomaticRetries()
                .build();
    }

}
