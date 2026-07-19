package com.whatsoeversky.minder.sftp.plugins;

import com.alibaba.fastjson.annotation.JSONField;
import com.whatsoeversky.minder.sftp.support.NestedJsonDeserializer;
import lombok.Data;

import java.util.Map;

@Data
public class ApiCallArg {
    private String method;
    private String url;
    @JSONField(deserializeUsing = NestedJsonDeserializer.class)
    private Map<String,String> headers;
    private String body;
    private Integer connectTimeout;
    private Integer readTimeout;
}
