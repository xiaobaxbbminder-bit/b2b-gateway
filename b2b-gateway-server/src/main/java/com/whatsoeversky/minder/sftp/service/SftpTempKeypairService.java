package com.whatsoeversky.minder.sftp.service;

import com.whatsoeversky.minder.sftp.dto.GenerateKeypairRespDto;
import com.whatsoeversky.minder.sftp.entity.SftpTempKeypair;
import com.whatsoeversky.minder.sftp.repository.SftpTempKeypairRepository;
import com.whatsoeversky.minder.utils.MyKeyPair;
import com.whatsoeversky.minder.utils.SSHDKeyUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Set;

@Service
public class SftpTempKeypairService {

    private static final Set<String> SUPPORTED_KEY_TYPES = Set.of("RSA", "ECDSA", "ED25519");

    @Autowired
    private SftpTempKeypairRepository sftpTempKeypairRepository;

    public GenerateKeypairRespDto generateKeypair(String keyType) {
        if (keyType == null || !SUPPORTED_KEY_TYPES.contains(keyType.toUpperCase())) {
            throw new RuntimeException("不支持的密钥类型: " + keyType + "，可选值: RSA, ECDSA, ED25519");
        }

        MyKeyPair keyPair = switch (keyType.toUpperCase()) {
            case "RSA" -> SSHDKeyUtils.generateRSAKeyPair(2048, "");
            case "ECDSA" -> SSHDKeyUtils.generateEcdsaKeyPair(256, "");
            case "ED25519" -> SSHDKeyUtils.generateEd25519KeyPair(256, "");
            default -> throw new RuntimeException("不支持的密钥类型: " + keyType);
        };

        SftpTempKeypair tempKeypair = SftpTempKeypair.builder()
                .keyType(keyType.toUpperCase())
                .publicKey(keyPair.getPublicKey().trim())
                .privateKey(keyPair.getPrivateKey())
                .createdAt(LocalDateTime.now())
                .build();
        SftpTempKeypair saved = sftpTempKeypairRepository.save(tempKeypair);

        return GenerateKeypairRespDto.builder()
                .id(saved.getId())
                .keyType(saved.getKeyType())
                .publicKey(saved.getPublicKey())
                .build();
    }
}
