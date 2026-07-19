package com.whatsoeversky.minder.sftp.plugins;

import lombok.Data;

import java.util.Map;

@Data
public class ApiCallArg {
    private String method;
    private String url;
    private Map<String,String> headers;
    private String body;
    private Integer connectTimeout;
    private Integer readTimeout;
}
