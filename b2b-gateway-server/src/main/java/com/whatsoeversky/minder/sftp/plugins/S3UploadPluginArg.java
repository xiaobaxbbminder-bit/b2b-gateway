package com.whatsoeversky.minder.sftp.plugins;

import lombok.Data;

@Data
public class S3UploadPluginArg {
    private String endpoint;
    private String bucket;
    private String region;
    private String accessKey;
    private String secretKey;
    private String targetPrefix;
    private Boolean useHttps;
    private Boolean pathStyle;
}
