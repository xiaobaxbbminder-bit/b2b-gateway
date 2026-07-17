package com.whatsoeversky.minder.sftp.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Document(collection = "sftp_service_configs")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SftpServiceConfig {
    @Id
    private String id;
    private String serviceId;
    private String userId;

    // 服务端-触发配置
    private String triggerType;
    private String watchPath;
    private Boolean compatibilityMode;

    // 客户端-调用配置
    private String invokeMode;
    private DataSource dataSource;
    private String fileFilter;

    // 插件配置
    private List<PluginConfig> plugins;

    // 告警配置
    private Boolean alertEnabled;
    private String alertLevel;
    private Integer failureThreshold;
    private String alertMessageTemplate;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PluginConfig {
        private String name;
        private Boolean enabled;
        private String execNode;
        private Map<String, Object> args;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DataSource {
        private String type;
        private Map<String, Object> args;
    }
}
