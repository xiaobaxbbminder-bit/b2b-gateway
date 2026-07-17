package com.whatsoeversky.minder.sftp.repository;

import com.whatsoeversky.minder.sftp.entity.SftpOperationLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface SftpOperationLogRepository extends MongoRepository<SftpOperationLog, String> {
    SftpOperationLog save(SftpOperationLog sftpOperationLog);
    
    Page<SftpOperationLog> findAllByOrderByStartTimeDesc(Pageable pageable);
}
