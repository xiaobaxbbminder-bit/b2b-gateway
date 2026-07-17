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
public class SftpUserUpdateReqDto {
    private String username;
    private Boolean enabled;
    private String userType;
    private String publicKey;
    private String keypairId;

    private String filesystemType;
    private SftpPermission permissions;
    private Long quotaBytes;

    private String remoteHost;
    private Integer remotePort;
    private String hostKeyAlgorithm;
    private String publicKeyAlgorithm;
    private String kexAlgorithm;
    private String encryptAlgorithm;
}
