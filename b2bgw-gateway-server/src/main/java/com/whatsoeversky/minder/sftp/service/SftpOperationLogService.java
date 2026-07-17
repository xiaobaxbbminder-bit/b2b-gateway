package com.whatsoeversky.minder.sftp.service;

import com.whatsoeversky.minder.sftp.entity.SftpOperationDetailLog;
import com.whatsoeversky.minder.sftp.entity.SftpOperationLog;
import com.whatsoeversky.minder.sftp.repository.SftpOperationDetailLogRepository;
import com.whatsoeversky.minder.sftp.repository.SftpOperationLogRepository;
import com.whatsoeversky.minder.sftp.server.enums.SftpOperationLogStatus;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

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

    public String saveOperationLog(String username, String clientAddress, String action, String filePath, Long fileSize, String description) {
        SftpOperationLog operationLog = SftpOperationLog.builder()
                .action(action)
                .filePath(filePath)
                .username(username)
                .clientAddress(clientAddress)
                .startTime(LocalDateTime.now())
                .status(SftpOperationLogStatus.PENDING.name())
                .fileSize(fileSize)
                .description(description)
                .build();
        SftpOperationLog insert = sftpOperationLogRepository.save(operationLog);
        SftpOperationDetailLog detailLog = SftpOperationDetailLog.builder()
                .logId(insert.getId())
                .logTime(LocalDateTime.now())
                .action(action)
                .status(SftpOperationLogStatus.PENDING.name())
                .description(description)
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
