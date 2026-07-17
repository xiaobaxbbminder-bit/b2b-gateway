package com.whatsoeversky.minder.sftp.client.service;

import com.whatsoeversky.minder.sftp.client.dto.SftpApiBatchReqDto;

public interface SftpClientService {
    void downloadBatch(SftpApiBatchReqDto reqDto);

    void uploadBatch(SftpApiBatchReqDto reqDto);
}
