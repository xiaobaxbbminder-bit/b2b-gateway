package com.whatsoeversky.minder.sftp.client.datasource.handler;

import com.whatsoeversky.minder.sftp.client.dto.SftpApiBatchReqDto;
import com.whatsoeversky.minder.sftp.entity.SftpServiceConfig;
import com.whatsoeversky.minder.sftp.support.FileMetadata;
import com.whatsoeversky.minder.sftp.support.FileRunContext;
import reactor.core.publisher.Flux;

public interface DataSourceHandler {
    String getType();

    Flux<FileMetadata> retrieveFileMetadataStream(SftpApiBatchReqDto reqDto, SftpServiceConfig sftpServiceConfig);

    void processDownload(FileRunContext fileRunContext, FileMetadata fileMetaData);

    void processUpload(FileRunContext fileRunContext, FileMetadata fileMetaData);
}
