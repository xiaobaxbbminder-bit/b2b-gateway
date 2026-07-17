package com.whatsoeversky.minder.sftp.service;

import com.whatsoeversky.minder.sftp.entity.SftpPluginDefinition;
import com.whatsoeversky.minder.sftp.entity.SftpPluginDefinition.VisibilityRule;
import com.whatsoeversky.minder.sftp.repository.SftpPluginDefinitionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class SftpPluginDefinitionService {

    @Autowired
    private SftpPluginDefinitionRepository sftpPluginDefinitionRepository;

    public List<SftpPluginDefinition> findAll() {
        return sftpPluginDefinitionRepository.findAll();
    }

    public SftpPluginDefinition findById(String id) {
        return sftpPluginDefinitionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("插件定义不存在: " + id));
    }

    public SftpPluginDefinition findByName(String name) {
        return sftpPluginDefinitionRepository.findByName(name)
                .orElseThrow(() -> new RuntimeException("插件定义不存在: " + name));
    }

    public SftpPluginDefinition save(SftpPluginDefinition definition) {
        return sftpPluginDefinitionRepository.save(definition);
    }

    public void deleteById(String id) {
        sftpPluginDefinitionRepository.deleteById(id);
    }

    public List<SftpPluginDefinition> findAvailable(String userType, String triggerType, String invokeMode) {
        List<SftpPluginDefinition> all = sftpPluginDefinitionRepository.findAll();
        return all.stream()
                .filter(pd -> pd.getVisibility() != null
                        && pd.getVisibility().stream().anyMatch(v -> matches(v, userType, triggerType, invokeMode)))
                .peek(pd -> {
                    List<String> execNodes = pd.getVisibility().stream()
                            .filter(v -> matches(v, userType, triggerType, invokeMode))
                            .map(VisibilityRule::getExecNodes)
                            .filter(nodes -> nodes != null)
                            .flatMap(List::stream)
                            .distinct()
                            .collect(Collectors.toList());
                    pd.setVisibility(List.of(VisibilityRule.builder()
                            .userType(userType)
                            .triggerType(triggerType)
                            .invokeMode(invokeMode)
                            .execNodes(execNodes)
                            .build()));
                })
                .collect(Collectors.toList());
    }

    private boolean matches(VisibilityRule rule, String userType, String triggerType, String invokeMode) {
        if (!rule.getUserType().equals(userType)) {
            return false;
        }
        if ("server".equals(userType)) {
            return rule.getTriggerType() != null && rule.getTriggerType().equals(triggerType);
        } else {
            return rule.getInvokeMode() != null && rule.getInvokeMode().equals(invokeMode);
        }
    }
}
