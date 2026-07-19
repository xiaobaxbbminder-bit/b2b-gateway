package com.whatsoeversky.minder.sftp.server.listener.handler.flow;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.whatsoeversky.minder.sftp.entity.SftpServiceConfig;
import com.whatsoeversky.minder.sftp.enums.SftpOperationLogStatus;
import com.whatsoeversky.minder.sftp.plugins.Plugin;
import com.whatsoeversky.minder.sftp.service.SftpOperationLogService;
import com.whatsoeversky.minder.sftp.support.FileRunContext;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Slf4j
@Component
public class PluginChain {

    @Resource
    private ObjectMapper objectMapper;

    @Resource
    private List<Plugin> plugins;

    @Resource
    private SftpOperationLogService sftpOperationLogService;


    public void doPluginChain(FileRunContext fileRunContext, List<SftpServiceConfig> sftpServiceConfigList, String executeNode) {
        if (CollectionUtils.isEmpty(sftpServiceConfigList) || fileRunContext.getFile() == null) {
            return;
        }
        Path file = fileRunContext.getFile();
        Map<String, Plugin> pluginMap = new HashMap<>();
        for (Plugin plugin : plugins) {
            pluginMap.put(plugin.getPluginName(), plugin);
        }
        List<SftpServiceConfig> filtered = sftpServiceConfigList.stream()
                .filter(config -> hasExecNodePlugin(config, executeNode))
                .filter(config -> matchWatchPath(config, file))
                .toList();
        for (SftpServiceConfig config : filtered) {
            executePlugins(config, executeNode, fileRunContext, pluginMap);
        }
    }

    private boolean hasExecNodePlugin(SftpServiceConfig config, String execNode) {
        List<SftpServiceConfig.PluginConfig> pluginConfigs = config.getPlugins();
        if (CollectionUtils.isEmpty(pluginConfigs)) {
            return false;
        }
        return pluginConfigs.stream()
                .anyMatch(p -> Boolean.TRUE.equals(p.getEnabled()) && execNode.equals(p.getExecNode()));
    }

    private boolean matchWatchPath(SftpServiceConfig config, Path file) {
        String watchPath = config.getWatchPath();
        if (!StringUtils.hasLength(watchPath)) {
            return true;
        }
        Path fileParent = file.getParent();
        return fileParent != null && fileParent.startsWith(watchPath);
    }

    private void executePlugins(SftpServiceConfig config, String execNode, FileRunContext fileRunContext, Map<String, Plugin> pluginMap) {
        String logId = fileRunContext.getLogId();
        List<SftpServiceConfig.PluginConfig> execPlugins = config.getPlugins().stream()
                .filter(p -> Boolean.TRUE.equals(p.getEnabled()) && execNode.equals(p.getExecNode()))
                .toList();
        boolean hasError = false;
        for (SftpServiceConfig.PluginConfig pluginConfig : execPlugins) {
            Plugin plugin = pluginMap.get(pluginConfig.getName());
            if (plugin == null) {
                log.warn("plugin not found: {}", pluginConfig.getName());
                if (logId != null) {
                    sftpOperationLogService.updateOperationLog(logId, pluginConfig.getName().toUpperCase(Locale.ROOT), SftpOperationLogStatus.ERROR.name(), "插件未找到: " + pluginConfig.getName());
                }
                hasError = true;
                break;
            }
            String pluginName = plugin.getPluginName();
            Map<String, Object> ctxBefore = new HashMap<>(fileRunContext.getContextVariables());
            if (logId != null) {
                String ctxJson = toJson(ctxBefore);
                sftpOperationLogService.updateOperationLog(logId, pluginName.toUpperCase(Locale.ROOT), SftpOperationLogStatus.PENDING.name(), "开始执行插件: " + pluginName, ctxJson);
            }
            try {
                String argsJson = objectMapper.writeValueAsString(pluginConfig.getArgs());
                plugin.execute(fileRunContext, argsJson);
                if (logId != null) {
                    String ctxJson = toJson(fileRunContext.getContextVariables());
                    sftpOperationLogService.updateOperationLog(logId, pluginName.toUpperCase(Locale.ROOT), SftpOperationLogStatus.SUCCESS.name(), "插件执行成功: " + pluginName, ctxJson);
                }
            } catch (Exception e) {
                log.error("plugin execute error: {}", pluginName, e);
                if (logId != null) {
                    String ctxJson = toJson(fileRunContext.getContextVariables());
                    sftpOperationLogService.updateOperationLog(logId, pluginName.toUpperCase(Locale.ROOT), SftpOperationLogStatus.ERROR.name(), "插件执行失败: " + e.getMessage(), ctxJson);
                }
                hasError = true;
                break;
            }
        }
        if (!hasError && logId != null) {
            sftpOperationLogService.updateOperationLog(logId, null, SftpOperationLogStatus.COMPLETED.name(), "插件链执行完成");
        }
    }

    private String toJson(Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (Exception e) {
            log.warn("failed to serialize context", e);
            return "{}";
        }
    }
}
