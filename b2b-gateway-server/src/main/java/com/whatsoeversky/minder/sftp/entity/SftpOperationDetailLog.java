package com.whatsoeversky.minder.sftp.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document(collection = "sftp_operation_details_logs")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SftpOperationDetailLog {
    @Id
    private String id;
    private String logId;
    private String action;
    private String status;
    private String description;
    private String context;
    private LocalDateTime logTime;
}
