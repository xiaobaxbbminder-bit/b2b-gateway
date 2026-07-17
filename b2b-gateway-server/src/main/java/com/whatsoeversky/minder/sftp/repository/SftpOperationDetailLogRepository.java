package com.whatsoeversky.minder.sftp.repository;

import com.whatsoeversky.minder.sftp.entity.SftpOperationDetailLog;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface SftpOperationDetailLogRepository extends MongoRepository<SftpOperationDetailLog, String> {
    SftpOperationDetailLog save(SftpOperationDetailLog sftpOperationDetailLog);
    List<SftpOperationDetailLog> findByLogIdOrderByLogTimeAsc(String logId);
}
