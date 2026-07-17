package com.whatsoeversky.minder.common;

import lombok.Getter;

public enum ResultCode {
    SUCCESS(20000, "success"),
    UNAUTHORIZED(40100, "认证失败"),
    SERVER_ERROR(50000, "服务器错误"),
    ;

    @Getter
    private int code;
    @Getter
    private String desc;

    ResultCode(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }
}
