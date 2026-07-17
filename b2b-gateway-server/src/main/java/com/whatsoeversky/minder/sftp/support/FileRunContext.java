package com.whatsoeversky.minder.sftp.support;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;

@Data
public class FileRunContext {
    private Map<String, Object> contextVariables = new LinkedHashMap<>();
    private Path file;
    private String logId;

    public void putContextVariables(String pluginName, ContextVariable result) {
        contextVariables.put(pluginName, result);
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ContextVariable {
        private Object args;
        private Object res;
    }
}
