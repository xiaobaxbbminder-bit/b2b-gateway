package com.whatsoeversky.minder.sftp.plugins;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.whatsoeversky.minder.helper.S3ClientHelper;
import com.whatsoeversky.minder.helper.s3.S3PutObjectRequest;
import com.whatsoeversky.minder.helper.s3.S3PutObjectResponse;
import com.whatsoeversky.minder.sftp.support.FileRunContext;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
public class S3UploadPlugin implements Plugin {

    @Resource
    private ObjectMapper objectMapper;

    @Resource
    private S3ClientHelper s3ClientHelper;

    @Override
    public String getPluginName() {
        return "s3_upload";
    }

    @Override
    public void execute(FileRunContext context, String args) throws IOException {
        S3UploadPluginArg arg = objectMapper.readValue(args, S3UploadPluginArg.class);
        Path file = context.getFile();

        String filename = file.getFileName().toString();
        String prefix = arg.getTargetPrefix() != null ? arg.getTargetPrefix() : "";
        String key = prefix.isEmpty() ? filename : prefix.endsWith("/") ? prefix + filename : prefix + "/" + filename;

        S3PutObjectRequest request = new S3PutObjectRequest();
        request.setEndpoint(arg.getEndpoint());
        request.setBucket(arg.getBucket());
        request.setRegion(arg.getRegion());
        request.setAccessKey(arg.getAccessKey());
        request.setSecretKey(arg.getSecretKey());
        request.setFilePath(file);
        request.setKey(key);
        request.setUseHttps(arg.getUseHttps() == null || arg.getUseHttps());
        request.setPathStyle(arg.getPathStyle() != null && arg.getPathStyle());

        try {
            S3PutObjectResponse response = s3ClientHelper.putObject(request);

            log.info("s3 upload success, bucket: {}, key: {}, etag: {}", arg.getBucket(), key, response.getEtag());

            Map<String, Object> result = new HashMap<>();
            result.put("bucket", arg.getBucket());
            result.put("key", key);
            result.put("etag", response.getEtag());
            context.putContextVariables(getPluginName(),
                    FileRunContext.ContextVariable.builder().args(arg).res(result).build());
        } catch (Exception e) {
            log.error("s3 upload error, bucket: {}, key: {}", arg.getBucket(), key, e);
            throw new IOException("s3 upload failed: " + e.getMessage());
        }
    }
}
