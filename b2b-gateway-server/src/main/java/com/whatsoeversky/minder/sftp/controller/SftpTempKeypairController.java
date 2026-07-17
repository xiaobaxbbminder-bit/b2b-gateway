package com.whatsoeversky.minder.sftp.controller;

import com.whatsoeversky.minder.common.Result;
import com.whatsoeversky.minder.sftp.dto.GenerateKeypairReqDto;
import com.whatsoeversky.minder.sftp.dto.GenerateKeypairRespDto;
import com.whatsoeversky.minder.sftp.service.SftpTempKeypairService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class SftpTempKeypairController {

    @Autowired
    private SftpTempKeypairService sftpTempKeypairService;

    @PostMapping("/keypairs/generate")
    public Result<GenerateKeypairRespDto> generateKeypair(@RequestBody GenerateKeypairReqDto dto) {
        GenerateKeypairRespDto resp = sftpTempKeypairService.generateKeypair(dto.getKeyType());
        return Result.success(resp);
    }
}
