package com.whatsoeversky.minder.sftp.client.datasource.handler;

import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;
import com.whatsoeversky.minder.sftp.client.dto.SftpApiBatchReqDto;
import com.whatsoeversky.minder.sftp.entity.SftpServiceConfig;
import com.whatsoeversky.minder.sftp.support.FileMetadata;
import com.whatsoeversky.minder.sftp.support.FileRunContext;
import com.whatsoeversky.minder.utils.AesUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Vector;

@Component
public class SftpDataSourceHandler implements DataSourceHandler {
    @Value("${sftp.aes-key}")
    private String aesKey;

    @Override
    public String getType() {
        return "SFTP";
    }

    @Override
    public Flux<FileMetadata> retrieveFileMetadataStream(SftpApiBatchReqDto reqDto,
                                                         SftpServiceConfig sftpServiceConfig) {
        return Flux.create(sink -> {
            Session session = null;
            ChannelSftp channel = null;
            try {
                Map<String, Object> args = sftpServiceConfig.getDataSource().getArgs();
                session = createSession(args);
                channel = (ChannelSftp) session.openChannel("sftp");
                channel.connect();

                String remotePath = (String) args.getOrDefault("remotePath", "/");
                Vector<ChannelSftp.LsEntry> entries = channel.ls(remotePath);
                for (ChannelSftp.LsEntry entry : entries) {
                    String filename = entry.getFilename();
                    if (filename.equals(".") || filename.equals("..")) continue;
                    if (entry.getAttrs().isDir()) continue;
                    sink.next(FileMetadata.builder()
                            .fileName(filename)
                            .fileSize(entry.getAttrs().getSize())
                            .lastModified(entry.getAttrs().getMTime() * 1000L)
                            .build());
                }
                sink.complete();
            } catch (Exception e) {
                sink.error(e);
            } finally {
                if (channel != null) channel.disconnect();
                if (session != null) session.disconnect();
            }
        });
    }

    @Override
    public void processDownload(FileRunContext fileRunContext, FileMetadata fileMetaData) {
        Session session = null;
        ChannelSftp channel = null;
        try {
            Map<String, Object> args = (Map<String, Object>) fileRunContext.getContextVariables().get("dataSourceArgs");
            String remotePath = (String) args.getOrDefault("remotePath", "/");

            session = createSession(args);
            channel = (ChannelSftp) session.openChannel("sftp");
            channel.connect();

            Path tempFile = Files.createTempFile("sftp-", fileMetaData.getFileName());
            String remoteFile = remotePath.endsWith("/") ? remotePath + fileMetaData.getFileName() : remotePath + "/" + fileMetaData.getFileName();
            try (var out = Files.newOutputStream(tempFile)) {
                channel.get(remoteFile, out);
            }
            fileRunContext.setFile(tempFile);
        } catch (Exception e) {
            throw new RuntimeException("SFTP download failed: " + fileMetaData.getFileName(), e);
        } finally {
            if (channel != null) channel.disconnect();
            if (session != null) session.disconnect();
        }
    }

    @Override
    public void processUpload(FileRunContext fileRunContext, FileMetadata fileMetaData) {
        Session session = null;
        ChannelSftp channel = null;
        try {
            Map<String, Object> args = (Map<String, Object>) fileRunContext.getContextVariables().get("uploadTargetArgs");
            session = createSession(args);
            channel = (ChannelSftp) session.openChannel("sftp");
            channel.connect();

            try (var in = Files.newInputStream(fileRunContext.getFile())) {
                channel.put(in, fileMetaData.getFileName());
            }
        } catch (Exception e) {
            throw new RuntimeException("SFTP upload failed: " + fileMetaData.getFileName(), e);
        } finally {
            if (channel != null) channel.disconnect();
            if (session != null) session.disconnect();
        }
    }

    @SuppressWarnings("unchecked")
    private Session createSession(Map<String, Object> args) throws Exception {
        String host = (String) args.get("remoteHost");
        int port = args.get("remotePort") != null ? ((Number) args.get("remotePort")).intValue() : 22;
        String username = (String) args.get("username");
        String password = (String) args.get("password");
        String privateKey = (String) args.get("privateKey");

        JSch jsch = new JSch();
        if (privateKey != null && !privateKey.isEmpty()) {
            jsch.addIdentity("sftp-key", privateKey.getBytes(), null, null);
        }

        Session session = jsch.getSession(username, host, port);
        if (password != null && !password.isEmpty()) {
            session.setPassword(AesUtil.decrypt(password,aesKey));
        }
        session.setConfig("StrictHostKeyChecking", "no");
        session.connect();
        return session;
    }
}
