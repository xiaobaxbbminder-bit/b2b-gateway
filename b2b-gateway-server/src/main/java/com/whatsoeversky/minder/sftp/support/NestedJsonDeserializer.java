package com.whatsoeversky.minder.sftp.support;


import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.parser.DefaultJSONParser;
import com.alibaba.fastjson.parser.deserializer.ObjectDeserializer;

import java.lang.reflect.Type;

public class NestedJsonDeserializer implements ObjectDeserializer {
    @Override
    public <T> T deserialze(DefaultJSONParser parser, Type type, Object fieldName) {
        // 1. 先把当前字段作为普通的字符串解析出来
        String innerJsonStr = parser.parseObject(String.class);
        if (innerJsonStr == null || innerJsonStr.isEmpty()) {
            return null;
        }
        // 2. 将这个内部字符串再次反序列化为目标对象（这里目标对象是 Config）
        return JSON.parseObject(innerJsonStr, type);
    }

    @Override
    public int getFastMatchToken() {
        return com.alibaba.fastjson.parser.JSONToken.LITERAL_STRING;
    }
}