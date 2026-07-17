package com.whatsoeversky.minder.helper.s3;

import lombok.Data;

@Data
public class S3BaseRequest {
    private String accessKey;
    private String secretKey;
    private String endpoint;
    private String region;
    private boolean pathStyle;
    private boolean useHttps;
}
