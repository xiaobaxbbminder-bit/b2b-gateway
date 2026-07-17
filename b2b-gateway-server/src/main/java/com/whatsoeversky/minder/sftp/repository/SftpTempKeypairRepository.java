package com.whatsoeversky.minder.sftp.repository;

import com.whatsoeversky.minder.sftp.entity.SftpTempKeypair;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface SftpTempKeypairRepository extends MongoRepository<SftpTempKeypair, String> {
}
