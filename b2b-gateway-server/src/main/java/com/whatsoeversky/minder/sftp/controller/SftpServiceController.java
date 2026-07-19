package com.whatsoeversky.minder.sftp.controller;

import com.whatsoeversky.minder.common.Result;
import com.whatsoeversky.minder.sftp.dto.SftpServiceCreateReqDto;
import com.whatsoeversky.minder.sftp.dto.SftpServiceRespDto;
import com.whatsoeversky.minder.sftp.dto.SftpServiceUpdateReqDto;
import com.whatsoeversky.minder.sftp.service.SftpServiceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/sftp")
public class SftpServiceController {

    @Autowired
    private SftpServiceService sftpServiceService;

    @GetMapping("/services")
    public Result<List<SftpServiceRespDto>> listServices() {
        return Result.success(sftpServiceService.findAll());
    }

    @GetMapping("/services/{id}")
    public Result<SftpServiceRespDto> getService(@PathVariable String id) {
        return Result.success(sftpServiceService.getService(id));
    }

    @PostMapping("/services")
    public Result<SftpServiceRespDto> createService(@RequestBody SftpServiceCreateReqDto dto) {
        return Result.success(sftpServiceService.createService(dto));
    }

    @PutMapping("/services/{id}")
    public Result<SftpServiceRespDto> updateService(@PathVariable String id, @RequestBody SftpServiceUpdateReqDto dto) {
        return Result.success(sftpServiceService.updateService(id, dto));
    }

    @DeleteMapping("/services/{id}")
    public Result<Void> deleteService(@PathVariable String id) {
        sftpServiceService.deleteService(id);
        return Result.success();
    }
}
