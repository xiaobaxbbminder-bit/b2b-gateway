package com.whatsoeversky.minder.sftp.client.controller;

import com.whatsoeversky.minder.sftp.client.service.SftpClientService;
import com.whatsoeversky.minder.sftp.client.dto.SftpApiBatchReqDto;
import com.whatsoeversky.minder.common.Result;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/sftp/client")
public class SftpClientController {
    @Resource
    private SftpClientService sftpApiService;

    @PostMapping("/download")
    public void download(HttpServletResponse response) {

    }

    @PostMapping("/upload")
    public void upload(HttpServletRequest request) {

    }

    @PostMapping("/download/batch")
    public Result<Void> downloadBatch(@RequestBody SftpApiBatchReqDto reqDto) {
        sftpApiService.downloadBatch(reqDto);
        return Result.success();
    }


    @PostMapping("/upload/batch")
    public Result<Void> uploadBatch(@RequestBody SftpApiBatchReqDto reqDto) {
        sftpApiService.uploadBatch(reqDto);
        return Result.success();
    }

}
