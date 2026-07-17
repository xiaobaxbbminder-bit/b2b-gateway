package com.whatsoeversky.minder.sftp.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;
import java.util.Map;

@Document(collection = "sftp_plugin_definitions")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SftpPluginDefinition {
    @Id
    private String id;
    private String name;
    private String label;
    private String description;
    private List<VisibilityRule> visibility;
    private Map<String, Object> defaults;
    private List<String> requiredFields;
    private Map<String, String> fieldLabels;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class VisibilityRule {
        private String userType;
        private String triggerType;
        private String invokeMode;
        private List<String> execNodes;
    }
}
