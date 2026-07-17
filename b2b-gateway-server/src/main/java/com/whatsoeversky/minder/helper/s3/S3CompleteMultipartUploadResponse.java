package com.whatsoeversky.minder.helper.s3;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class S3CompleteMultipartUploadResponse {
    private String location;
    private String bucket;
    private String key;
    private String etag;
}
