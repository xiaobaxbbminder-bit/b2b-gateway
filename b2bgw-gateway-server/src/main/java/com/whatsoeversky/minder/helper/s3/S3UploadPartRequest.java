package com.whatsoeversky.minder.helper.s3;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.nio.file.Path;

@EqualsAndHashCode(callSuper = true)
@Data
public class S3UploadPartRequest extends S3BaseRequest {
    private String bucket;
    private String key;
    private int partNumber;
    private String uploadId;
    private Path filePath;
    private long fileOffset;
    private int partSize;
}
