package com.whatsoeversky.minder.sftp.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document(collection = "sftp_operation_logs")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SftpOperationLog {
    @Id
    private String id;
    private String filePath;
    private String username;
    private String clientAddress;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String status;
    private String action;
    private String description;
    private Long fileSize;
}
