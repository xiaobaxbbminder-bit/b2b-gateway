package com.whatsoeversky.minder.helper.s3;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.nio.file.Path;

@EqualsAndHashCode(callSuper = true)
@Data
public class S3MultipartUploadRequest extends S3BaseRequest {
    private String bucket;
    private String key;
    private Path filePath;
    private String contentType;
    private Long partSize;
}
