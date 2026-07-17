package com.whatsoeversky.minder.sftp.client.datasource.handler;

import com.whatsoeversky.minder.sftp.client.dto.SftpApiBatchReqDto;
import com.whatsoeversky.minder.sftp.entity.SftpServiceConfig;
import com.whatsoeversky.minder.helper.S3ClientHelper;
import com.whatsoeversky.minder.helper.s3.S3GetObjectRequest;
import com.whatsoeversky.minder.helper.s3.S3ListObjectRequest;
import com.whatsoeversky.minder.sftp.support.FileMetadata;
import com.whatsoeversky.minder.sftp.support.FileRunContext;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;
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

                var response = s3ClientHelper.listObject(request);
                for (var item : response.getItems()) {
                    sink.next(FileMetadata.builder()
                            .fileName(item.getKey())
                            .fileSize(item.getSize())
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
            request.setKey(fileMetaData.getFileName());

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
    }
}
