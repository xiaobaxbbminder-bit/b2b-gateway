package com.whatsoeversky.minder.sftp.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SftpServiceUpdateReqDto {
    private String name;
    private String userId;
    private Boolean enabled;
}
