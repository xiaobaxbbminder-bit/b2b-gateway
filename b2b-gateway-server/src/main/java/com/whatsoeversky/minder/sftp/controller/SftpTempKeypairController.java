package com.whatsoeversky.minder.sftp.controller;

import com.whatsoeversky.minder.common.Result;
import com.whatsoeversky.minder.sftp.dto.SftpGenerateKeypairReqDto;
import com.whatsoeversky.minder.sftp.dto.SftpGenerateKeypairRespDto;
import com.whatsoeversky.minder.sftp.service.SftpTempKeypairService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/sftp")
public class SftpTempKeypairController {

    @Autowired
    private SftpTempKeypairService sftpTempKeypairService;

    @PostMapping("/keypairs/generate")
    public Result<SftpGenerateKeypairRespDto> generateKeypair(@RequestBody SftpGenerateKeypairReqDto dto) {
        SftpGenerateKeypairRespDto resp = sftpTempKeypairService.generateKeypair(dto.getKeyType());
        return Result.success(resp);
    }
}
