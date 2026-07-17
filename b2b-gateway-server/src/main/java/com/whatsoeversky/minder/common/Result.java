package com.whatsoeversky.minder.common;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Result<T> {
    private int code;
    private String message;
    private T data;

    public static <T> Result<T> success() {
        Result<T> res = new Result<>();
        res.code = ResultCode.SUCCESS.getCode();
        res.message = ResultCode.SUCCESS.getDesc();
        return res;
    }

    public static <T> Result<T> success(T data) {
        Result<T> res = new Result<>();
        res.code = ResultCode.SUCCESS.getCode();
        res.message = ResultCode.SUCCESS.getDesc();
        res.data = data;
        return res;
    }

    public static <T> Result<T> error(String message) {
        Result<T> res = new Result<>();
        res.code = ResultCode.SERVER_ERROR.getCode();
        res.message = message;
        return res;
    }

    public static <T> Result<T> error(ResultCode resultCode, String message) {
        Result<T> res = new Result<>();
        res.code = resultCode.getCode();
        res.message = message;
        return res;
    }
}
