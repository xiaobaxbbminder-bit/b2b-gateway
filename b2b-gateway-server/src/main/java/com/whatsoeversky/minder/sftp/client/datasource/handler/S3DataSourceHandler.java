package com.whatsoeversky.minder.sftp.client.datasource.handler;

import com.whatsoeversky.minder.sftp.client.dto.SftpApiBatchReqDto;
import com.whatsoeversky.minder.sftp.entity.SftpServiceConfig;
import com.whatsoeversky.minder.helper.S3ClientHelper;
import com.whatsoeversky.minder.helper.s3.S3GetObjectRequest;
import com.whatsoeversky.minder.helper.s3.S3ListObjectRequest;
import com.whatsoeversky.minder.helper.s3.S3PutObjectRequest;
import com.whatsoeversky.minder.sftp.support.FileMetadata;
import com.whatsoeversky.minder.sftp.support.FileRunContext;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import reactor.core.publisher.Flux;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

@Component
public class S3DataSourceHandler implements DataSourceHandler {

    @Resource
    private S3ClientHelper s3ClientHelper;

    @Override
    public String getType() {
        return "S3";
    }

    @Override
    public Flux<FileMetadata> retrieveFileMetadataStream(SftpApiBatchReqDto reqDto,
                                                         SftpServiceConfig sftpServiceConfig) {
        return Flux.create(sink -> {
            try {
                Map<String, Object> args = sftpServiceConfig.getDataSource().getArgs();
                S3ListObjectRequest request = new S3ListObjectRequest();
                request.setEndpoint((String) args.get("endpoint"));
                request.setBucket((String) args.get("bucket"));
                request.setRegion((String) args.get("region"));
                request.setAccessKey((String) args.get("accessKey"));
                request.setSecretKey((String) args.get("secretKey"));
                request.setUseHttps(args.get("useHttps") != null && (Boolean) args.get("useHttps"));
                request.setPathStyle(args.get("pathStyle") != null && (Boolean) args.get("pathStyle"));

                String prefix = reqDto.getFilename();
                if (prefix != null && !prefix.isEmpty()) {
                    request.setPrefix(prefix);
                }

                if (!Boolean.TRUE.equals(reqDto.getRecursive())) {
                    request.setDelimiter("/");
                }

                var response = s3ClientHelper.listObject(request);
                for (var item : response.getItems()) {
                    String key = item.getKey();
                    String relativePath = key;
                    if (prefix != null && !prefix.isEmpty() && key.startsWith(prefix)) {
                        relativePath = key.substring(prefix.length());
                    }
                    int lastSlash = relativePath.lastIndexOf('/');
                    String fileName = lastSlash >= 0 ? relativePath.substring(lastSlash + 1) : relativePath;
                    sink.next(FileMetadata.builder()
                            .fileName(fileName)
                            .fileSize(item.getSize())
                            .lastModified(item.getLastModifiedInstant() != null ? item.getLastModifiedInstant().toEpochMilli() : null)
                            .relativePath(relativePath)
                            .build());
                }
                sink.complete();
            } catch (Exception e) {
                sink.error(e);
            }
        });
    }

    @Override
    public void processDownload(FileRunContext fileRunContext, FileMetadata fileMetaData) {
        try {
            Map<String, Object> args = (Map<String, Object>) fileRunContext.getContextVariables().get("dataSourceArgs");
            S3GetObjectRequest request = new S3GetObjectRequest();
            request.setEndpoint((String) args.get("endpoint"));
            request.setBucket((String) args.get("bucket"));
            request.setRegion((String) args.get("region"));
            request.setAccessKey((String) args.get("accessKey"));
            request.setSecretKey((String) args.get("secretKey"));
            request.setUseHttps(args.get("useHttps") != null && (Boolean) args.get("useHttps"));
            request.setPathStyle(args.get("pathStyle") != null && (Boolean) args.get("pathStyle"));

            String prefix = (String) args.get("sourcePrefix");
            String key;
            if (StringUtils.hasLength(prefix) && fileMetaData.getRelativePath() != null) {
                key = prefix + fileMetaData.getRelativePath();
            } else if (fileMetaData.getRelativePath() != null) {
                key = fileMetaData.getRelativePath();
            } else {
                key = fileMetaData.getFileName();
            }
            request.setKey(key);

            var response = s3ClientHelper.getObject(request);
            Path tempFile = Files.createTempFile("s3-", fileMetaData.getFileName());
            Files.write(tempFile, response.getData());
            fileRunContext.setFile(tempFile);
        } catch (Exception e) {
            throw new RuntimeException("S3 download failed: " + fileMetaData.getFileName(), e);
        }
    }

    @Override
    public void processUpload(FileRunContext fileRunContext, FileMetadata fileMetaData) {
        try {
            Map<String, Object> args = (Map<String, Object>) fileRunContext.getContextVariables().get("uploadTargetArgs");
            S3PutObjectRequest request = new S3PutObjectRequest();
            request.setEndpoint((String) args.get("endpoint"));
            request.setBucket((String) args.get("bucket"));
            request.setRegion((String) args.get("region"));
            request.setAccessKey((String) args.get("accessKey"));
            request.setSecretKey((String) args.get("secretKey"));
            request.setUseHttps(args.get("useHttps") != null && (Boolean) args.get("useHttps"));
            request.setPathStyle(args.get("pathStyle") != null && (Boolean) args.get("pathStyle"));

            String targetPrefix = (String) args.get("targetPrefix");
            String relativePath = fileMetaData.getRelativePath();
            String key;
            if (relativePath != null && !relativePath.isEmpty()) {
                if (targetPrefix != null && !targetPrefix.isEmpty()) {
                    key = targetPrefix.endsWith("/") ? targetPrefix + relativePath : targetPrefix + "/" + relativePath;
                } else {
                    key = relativePath;
                }
            } else {
                String fileName = fileMetaData.getFileName();
                if (targetPrefix != null && !targetPrefix.isEmpty()) {
                    key = targetPrefix.endsWith("/") ? targetPrefix + fileName : targetPrefix + "/" + fileName;
                } else {
                    key = fileName;
                }
            }
            request.setKey(key);
            request.setFilePath(fileRunContext.getFile());
            s3ClientHelper.putObject(request);
        } catch (Exception e) {
            throw new RuntimeException("S3 upload failed: " + fileMetaData.getFileName(), e);
        }
    }
}
