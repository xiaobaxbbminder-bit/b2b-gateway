package com.whatsoeversky.minder.sftp.repository;

import com.whatsoeversky.minder.sftp.entity.SftpServiceConfig;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface SftpServiceConfigRepository extends MongoRepository<SftpServiceConfig, String> {
    Optional<SftpServiceConfig> findByServiceId(String serviceId);

    boolean existsByServiceId(String serviceId);

    List<SftpServiceConfig> findByUserId(String userId);
}

