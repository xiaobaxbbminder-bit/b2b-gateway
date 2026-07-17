package com.whatsoeversky.minder.sftp.repository;

import com.whatsoeversky.minder.sftp.entity.SftpPluginDefinition;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface SftpPluginDefinitionRepository extends MongoRepository<SftpPluginDefinition, String> {
    Optional<SftpPluginDefinition> findByName(String name);
}
