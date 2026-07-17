package com.whatsoeversky.minder.sftp.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document(collection = "sftp_users")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SftpUser {
    @Id
    private String id;
    private String username;
    private String password;
    private Boolean enabled;
    private Long usedBytes;
    private String userType;
    private String publicKey;
    private String privateKey;

    // 服务端账号：文件系统配置
    private String filesystemType;

    // 服务端账号：服务权限
    private SftpPermission permissions;

    // 服务端账号：存储配额
    private Long quotaBytes;

    // 客户端账号：远程服务器连接信息
    private String remoteHost;
    private Integer remotePort;

    // 客户端账号：密钥类型 (RSA/ECDSA/ED25519)
    private String keyType;

    // 客户端账号：认证算法
    private String hostKeyAlgorithm;
    private String publicKeyAlgorithm;
    private String kexAlgorithm;
    private String encryptAlgorithm;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
