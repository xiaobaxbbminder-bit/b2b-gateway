package com.whatsoeversky.minder.sftp.plugins;

import lombok.Data;

@Data
public class ApiCallArg {
    private String method;
    private String url;
    private String headers;
    private String body;
    private Integer connectTimeout;
    private Integer readTimeout;
}
