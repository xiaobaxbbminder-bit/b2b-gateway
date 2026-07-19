package com.whatsoeversky.minder.sftp.controller;

import com.whatsoeversky.minder.common.Result;
import com.whatsoeversky.minder.sftp.entity.SftpPluginDefinition;
import com.whatsoeversky.minder.sftp.service.SftpPluginDefinitionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/sftp/plugin-definitions")
public class SftpPluginDefinitionController {

    @Autowired
    private SftpPluginDefinitionService sftpPluginDefinitionService;

    @GetMapping
    public Result<List<SftpPluginDefinition>> list() {
        return Result.success(sftpPluginDefinitionService.findAll());
    }

    @GetMapping("/{id}")
    public Result<SftpPluginDefinition> getById(@PathVariable String id) {
        return Result.success(sftpPluginDefinitionService.findById(id));
    }

    @PostMapping
    public Result<SftpPluginDefinition> create(@RequestBody SftpPluginDefinition definition) {
        return Result.success(sftpPluginDefinitionService.save(definition));
    }

    @PutMapping("/{id}")
    public Result<SftpPluginDefinition> update(@PathVariable String id, @RequestBody SftpPluginDefinition definition) {
        definition.setId(id);
        return Result.success(sftpPluginDefinitionService.save(definition));
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable String id) {
        sftpPluginDefinitionService.deleteById(id);
        return Result.success();
    }

    @GetMapping("/available")
    public Result<List<SftpPluginDefinition>> getAvailable(
            @RequestParam String userType,
            @RequestParam(required = false) String triggerType,
            @RequestParam(required = false) String invokeMode) {
        return Result.success(sftpPluginDefinitionService.findAvailable(userType, triggerType, invokeMode));
    }
}
