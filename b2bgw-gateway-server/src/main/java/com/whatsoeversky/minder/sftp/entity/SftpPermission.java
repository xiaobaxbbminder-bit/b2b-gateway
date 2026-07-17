package com.whatsoeversky.minder.sftp.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SftpPermission {
    private boolean read;
    private boolean write;
    private boolean deleteFile;
    private boolean createFolder;
    private boolean deleteFolder;
    private boolean rename;
}
