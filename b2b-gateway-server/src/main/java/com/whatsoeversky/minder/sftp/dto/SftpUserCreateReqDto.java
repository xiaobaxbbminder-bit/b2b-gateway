package com.whatsoeversky.minder.sftp.dto;

import com.whatsoeversky.minder.sftp.entity.SftpPermission;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SftpUserCreateReqDto {
    private String username;
    private String password;
    private Boolean enabled;
    private String userType;
    private String publicKey;
    private String keypairId;

    // 服务端账号
    private String filesystemType;
    private SftpPermission permissions;
    private Boolean passwordLogin;

    // 客户端账号
    private String remoteHost;
    private Integer remotePort;
    private String hostKeyAlgorithm;
    private String publicKeyAlgorithm;
    private String kexAlgorithm;
    private String encryptAlgorithm;
}
