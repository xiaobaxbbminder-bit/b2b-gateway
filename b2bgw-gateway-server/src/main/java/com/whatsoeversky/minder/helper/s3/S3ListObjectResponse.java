package com.whatsoeversky.minder.helper.s3;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class S3ListObjectResponse {
    private String name;
    private String prefix;
    private String delimiter;
    private String marker;
    private Integer maxKeys;
    private boolean isTruncated;
    private List<String> commonPrefixes;
    private List<Item> items;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Item {
        private String key;
        private long size;
        private String etag;
        private String lastModified;
        private Instant lastModifiedInstant;
    }
}
