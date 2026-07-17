package com.whatsoeversky.minder.sftp.dto;

import com.whatsoeversky.minder.sftp.entity.SftpPermission;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SftpUserRespDto {
    private String id;
    private String username;
    private Boolean enabled;
    private Long usedBytes;
    private String userType;
    private String publicKey;

    // 服务端账号
    private String filesystemType;
    private SftpPermission permissions;
    private Long quotaBytes;

    // 客户端账号
    private String remoteHost;
    private Integer remotePort;
    private String keyType;
    private String hostKeyAlgorithm;
    private String publicKeyAlgorithm;
    private String kexAlgorithm;
    private String encryptAlgorithm;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
