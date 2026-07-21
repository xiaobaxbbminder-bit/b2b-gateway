package com.whatsoeversky.minder.sftp.support;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FileMetadata {
    private String fileName;
    private Long fileSize;
    private Long lastModified;
    private String relativePath;
    private Long lastModified;
}
