package com.whatsoeversky.minder.helper.s3;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class S3HeadObjectResponse {
    private String etag;
    private String contentType;
    private long contentLength;
    private String lastModified;
    private Instant lastModifiedInstant;
    private String versionId;
    private String storageClass;
    private String serverSideEncryption;
    private Map<String, String> userMetadata;
}
