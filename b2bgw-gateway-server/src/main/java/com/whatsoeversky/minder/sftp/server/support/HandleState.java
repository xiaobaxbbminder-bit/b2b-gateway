package com.whatsoeversky.minder.sftp.server.support;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HandleState {
    private String filePath;
    private String openMode;
    private long bytesTransferred;
}
