package com.whatsoeversky.minder.helper.s3;

import lombok.Data;

@Data
public class S3ListObjectRequest extends S3BaseRequest {
    private String bucket;
    private String prefix;
    private String delimiter;
    private String marker;
    private Integer maxKeys;
}
