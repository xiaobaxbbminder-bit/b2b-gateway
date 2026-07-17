package com.whatsoeversky.minder.sftp.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document(collection = "sftp_temp_keypair")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SftpTempKeypair {
    @Id
    private String id;
    private String keyType;
    private String publicKey;
    private String privateKey;
    private LocalDateTime createdAt;
}
