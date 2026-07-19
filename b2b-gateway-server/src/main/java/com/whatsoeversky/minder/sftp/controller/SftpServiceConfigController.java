package com.whatsoeversky.minder.sftp.controller;

import com.whatsoeversky.minder.common.Result;
import com.whatsoeversky.minder.sftp.dto.SftpServiceConfigReqDto;
import com.whatsoeversky.minder.sftp.dto.SftpServiceConfigRespDto;
import com.whatsoeversky.minder.sftp.service.SftpServiceConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/sftp")
public class SftpServiceConfigController {

    @Autowired
    private SftpServiceConfigService sftpServiceConfigService;

    @GetMapping("/services/{serviceId}/config")
    public Result<SftpServiceConfigRespDto> getConfig(@PathVariable String serviceId) {
        return Result.success(sftpServiceConfigService.getConfig(serviceId));
    }

    @PutMapping("/services/{serviceId}/config")
    public Result<SftpServiceConfigRespDto> saveConfig(@PathVariable String serviceId, @RequestBody SftpServiceConfigReqDto dto) {
        return Result.success(sftpServiceConfigService.saveConfig(serviceId, dto));
    }
}
