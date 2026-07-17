package com.whatsoeversky.minder.helper.s3;

import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class S3AbortMultipartUploadRequest extends S3BaseRequest {
    private String bucket;
    private String key;
    private String uploadId;
}
