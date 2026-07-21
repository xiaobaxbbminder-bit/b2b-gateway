package com.whatsoeversky.minder.sftp.client.dto;

import lombok.Data;

@Data
public class SftpApiBatchReqDto {
    private String serviceId;
    private String filename;
    private String startTime;
    private String endTime;
    private Boolean recursive;
}
