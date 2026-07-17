package com.whatsoeversky.minder.sftp.client.service.impl;

import com.whatsoeversky.minder.sftp.client.datasource.DataSourceSelector;
import com.whatsoeversky.minder.sftp.client.datasource.handler.DataSourceHandler;
import com.whatsoeversky.minder.sftp.client.service.SftpClientService;
import com.whatsoeversky.minder.sftp.client.dto.SftpApiBatchReqDto;
import com.whatsoeversky.minder.sftp.entity.SftpService;
import com.whatsoeversky.minder.sftp.entity.SftpServiceConfig;
import com.whatsoeversky.minder.sftp.entity.SftpUser;
import com.whatsoeversky.minder.sftp.repository.SftpServiceConfigRepository;
import com.whatsoeversky.minder.sftp.repository.SftpUserRepository;
import com.whatsoeversky.minder.sftp.service.SftpServiceService;
import com.whatsoeversky.minder.sftp.support.FileRunContext;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;

@Service
public class SftpClientServiceImpl implements SftpClientService {

    @Resource
    private SftpServiceConfigRepository sftpServiceConfigRepository;

    @Resource
    private SftpServiceService sftpServiceService;

    @Resource
    private SftpUserRepository sftpUserRepository;

    @Resource
    private DataSourceSelector dataSourceSelector;

    @Override
    public void downloadBatch(SftpApiBatchReqDto reqDto) {
        SftpServiceConfig config = sftpServiceConfigRepository.findByServiceId(reqDto.getServiceId())
                .orElseThrow(() -> new RuntimeException("服务配置不存在"));

        SftpService service = sftpServiceService.findById(reqDto.getServiceId())
                .orElseThrow(() -> new RuntimeException("服务不存在"));

        SftpUser partnerUser = sftpUserRepository.findById(service.getUserId())
                .orElseThrow(() -> new RuntimeException("用户不存在"));

        SftpServiceConfig.DataSource ds = config.getDataSource();
        DataSourceHandler handler = dataSourceSelector.getDataSourceHandler(ds.getType());
        if (handler == null) {
            throw new RuntimeException("不支持的数据源类型: " + ds.getType());
        }

        DataSourceHandler sftpHandler = dataSourceSelector.getDataSourceHandler("SFTP");
        if (sftpHandler == null) {
            throw new RuntimeException("SFTP 处理器不存在");
        }

        Map<String, Object> dataSourceArgs = new HashMap<>(ds.getArgs());
        Map<String, Object> uploadTargetArgs = buildPartnerSftpArgs(partnerUser);

        handler.retrieveFileMetadataStream(reqDto, config)
                .flatMap(metadata -> {
                    FileRunContext ctx = new FileRunContext();
                    ctx.getContextVariables().put("dataSourceArgs", dataSourceArgs);
                    handler.processDownload(ctx, metadata);

                    ctx.getContextVariables().put("uploadTargetArgs", uploadTargetArgs);
                    sftpHandler.processUpload(ctx, metadata);

                    cleanup(ctx);
                    return Mono.just(metadata);
                })
                .blockLast();
    }

    private Map<String, Object> buildPartnerSftpArgs(SftpUser user) {
        Map<String, Object> args = new HashMap<>();
        args.put("remoteHost", user.getRemoteHost());
        args.put("remotePort", user.getRemotePort());
        args.put("username", user.getUsername());
        args.put("password", user.getPassword());
        args.put("privateKey", user.getPrivateKey());
        return args;
    }

    private void cleanup(FileRunContext ctx) {
        if (ctx.getFile() != null) {
            try { Files.deleteIfExists(ctx.getFile()); } catch (Exception ignored) {}
        }
    }

    @Override
    public void uploadBatch(SftpApiBatchReqDto reqDto) {
        SftpServiceConfig config = sftpServiceConfigRepository.findByServiceId(reqDto.getServiceId())
                .orElseThrow(() -> new RuntimeException("服务配置不存在"));

        SftpService service = sftpServiceService.findById(reqDto.getServiceId())
                .orElseThrow(() -> new RuntimeException("服务不存在"));

        SftpUser partnerUser = sftpUserRepository.findById(service.getUserId())
                .orElseThrow(() -> new RuntimeException("用户不存在"));

        SftpServiceConfig.DataSource ds = config.getDataSource();
        DataSourceHandler targetHandler = dataSourceSelector.getDataSourceHandler(ds.getType());
        if (targetHandler == null) {
            throw new RuntimeException("不支持的目标数据源类型: " + ds.getType());
        }

        DataSourceHandler sftpHandler = dataSourceSelector.getDataSourceHandler("SFTP");
        if (sftpHandler == null) {
            throw new RuntimeException("SFTP 处理器不存在");
        }

        Map<String, Object> dataSourceArgs = new HashMap<>(ds.getArgs());
        Map<String, Object> partnerSftpArgs = buildPartnerSftpArgs(partnerUser);

        SftpServiceConfig partnerConfig = SftpServiceConfig.builder()
                .dataSource(SftpServiceConfig.DataSource.builder()
                        .type("SFTP")
                        .args(partnerSftpArgs)
                        .build())
                .build();

        sftpHandler.retrieveFileMetadataStream(reqDto, partnerConfig)
                .flatMap(metadata -> {
                    FileRunContext ctx = new FileRunContext();
                    ctx.getContextVariables().put("dataSourceArgs", partnerSftpArgs);
                    sftpHandler.processDownload(ctx, metadata);

                    ctx.getContextVariables().put("uploadTargetArgs", dataSourceArgs);
                    targetHandler.processUpload(ctx, metadata);

                    cleanup(ctx);
                    return Mono.just(metadata);
                })
                .blockLast();
    }
}
