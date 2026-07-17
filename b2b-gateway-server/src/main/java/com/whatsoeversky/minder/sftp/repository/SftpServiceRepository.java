package com.whatsoeversky.minder.sftp.repository;

import com.whatsoeversky.minder.sftp.entity.SftpService;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface SftpServiceRepository extends MongoRepository<SftpService, String> {
    List<SftpService> findByUserId(String userId);
    boolean existsByName(String name);
}
