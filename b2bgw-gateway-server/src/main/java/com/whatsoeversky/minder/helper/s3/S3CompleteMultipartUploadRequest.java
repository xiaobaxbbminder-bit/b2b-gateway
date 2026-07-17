package com.whatsoeversky.minder.helper.s3;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
public class S3CompleteMultipartUploadRequest extends S3BaseRequest {
    private String bucket;
    private String key;
    private String uploadId;
    private List<PartInfo> parts;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class PartInfo {
        private int partNumber;
        private String etag;
    }
}
