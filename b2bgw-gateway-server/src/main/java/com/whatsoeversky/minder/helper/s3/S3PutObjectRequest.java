package com.whatsoeversky.minder.helper.s3;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.nio.file.Path;

@EqualsAndHashCode(callSuper = true)
@Data
public class S3PutObjectRequest extends S3BaseRequest {
    private String bucket;
    private String key;
    private byte[] data;
    private Path filePath;
    private String contentType;
}
