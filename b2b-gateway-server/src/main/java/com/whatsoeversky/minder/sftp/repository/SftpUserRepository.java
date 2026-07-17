package com.whatsoeversky.minder.sftp.repository;

import com.whatsoeversky.minder.sftp.entity.SftpUser;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface SftpUserRepository extends MongoRepository<SftpUser, String> {

    Optional<SftpUser> findByUsername(String username);

    Optional<SftpUser> findByUsernameAndUserType(String username, String userType);

    boolean existsByUsername(String username);
}
