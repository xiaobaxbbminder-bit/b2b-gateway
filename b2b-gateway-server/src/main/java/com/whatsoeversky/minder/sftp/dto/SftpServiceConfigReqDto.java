package com.whatsoeversky.minder.sftp.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SftpServiceConfigReqDto {
    private String triggerType;
    private String watchPath;
    private String fileFilter;
    private Boolean compatibilityMode;

    private String invokeMode;
    private DataSourceDto dataSource;

    private List<PluginConfigDto> plugins;

    private Boolean alertEnabled;
    private String alertLevel;
    private Integer failureThreshold;
    private String alertMessageTemplate;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PluginConfigDto {
        private String name;
        private Boolean enabled;
        private String execNode;
        private Map<String, Object> args;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DataSourceDto {
        private String type;
        private Map<String, Object> args;
    }
}
