package com.whatsoeversky.minder.sftp.dto;

import lombok.Data;

@Data
public class SftpOperationLogSaveReqDto {
    private String sessionId;
    private String username;
    private String clientAddress;
    private String filePath;
    private Long fileSize;
    private String action;
    private String description;
    private String status;
}
