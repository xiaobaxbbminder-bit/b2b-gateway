package com.whatsoeversky.minder.sftp.service;

import com.whatsoeversky.minder.sftp.dto.SftpOperationLogSaveReqDto;
import com.whatsoeversky.minder.sftp.entity.SftpOperationDetailLog;
import com.whatsoeversky.minder.sftp.entity.SftpOperationLog;
import com.whatsoeversky.minder.sftp.repository.SftpOperationDetailLogRepository;
import com.whatsoeversky.minder.sftp.repository.SftpOperationLogRepository;
import com.whatsoeversky.minder.sftp.enums.SftpOperationLogStatus;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;

@Slf4j
@Service
public class SftpOperationLogService {

    @Resource
    private SftpOperationLogRepository sftpOperationLogRepository;

    @Resource
    private SftpOperationDetailLogRepository sftpOperationDetailLogRepository;

    @Resource
    private MongoTemplate mongoTemplate;

    public String saveOperationLog(SftpOperationLogSaveReqDto reqDto) {
        SftpOperationLog operationLog = SftpOperationLog.builder()
                .action(reqDto.getAction())
                .filePath(reqDto.getFilePath())
                .username(reqDto.getUsername())
                .clientAddress(reqDto.getClientAddress())
                .startTime(LocalDateTime.now())
                .status(StringUtils.hasLength(reqDto.getStatus()) ?
                        reqDto.getStatus() : SftpOperationLogStatus.PENDING.name())
                .fileSize(reqDto.getFileSize())
                .description(reqDto.getDescription())
                .build();
        SftpOperationLog insert = sftpOperationLogRepository.save(operationLog);
        SftpOperationDetailLog detailLog = SftpOperationDetailLog.builder()
                .logId(insert.getId())
                .logTime(LocalDateTime.now())
                .action(reqDto.getAction())
                .status(StringUtils.hasLength(reqDto.getStatus()) ?
                        reqDto.getStatus() : SftpOperationLogStatus.PENDING.name())
                .description(reqDto.getDescription())
                .build();
        sftpOperationDetailLogRepository.save(detailLog);
        return insert.getId();
    }

    public void updateOperationLog(String logId, String action, String status, String description) {
        updateOperationLog(logId, action, status, description, null);
    }

    public void updateOperationLog(String logId, String action, String status, String description, String context) {
        Query query = Query.query(Criteria.where("_id").is(logId));
        Update update = new Update();
        update.set("action", action);
        update.set("status", status);
        update.set("description", description);
        mongoTemplate.updateFirst(query, update, SftpOperationLog.class);
        SftpOperationDetailLog.SftpOperationDetailLogBuilder builder = SftpOperationDetailLog.builder()
                .logId(logId)
                .logTime(LocalDateTime.now())
                .action(action)
                .status(status)
                .description(description);
        if (context != null) {
            builder.context(context);
        }
        sftpOperationDetailLogRepository.save(builder.build());
    }

}
