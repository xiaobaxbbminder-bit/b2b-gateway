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
                boolean recursive = Boolean.TRUE.equals(reqDto.getRecursive());
                listFiles(channel, remotePath, "", recursive, sink);
                sink.complete();
            } catch (Exception e) {
                sink.error(e);
            } finally {
                if (channel != null) channel.disconnect();
                if (session != null) session.disconnect();
            }
        });
    }

    @SuppressWarnings("unchecked")
    private void listFiles(ChannelSftp channel, String basePath, String relativeDir, boolean recursive,
                           reactor.core.publisher.FluxSink<FileMetadata> sink) throws Exception {
        String dir = relativeDir.isEmpty() ? basePath : basePath + "/" + relativeDir;
        Vector<ChannelSftp.LsEntry> entries = channel.ls(dir);
        for (ChannelSftp.LsEntry entry : entries) {
            String filename = entry.getFilename();
            if (filename.equals(".") || filename.equals("..")) continue;

            String relativePath = relativeDir.isEmpty() ? filename : relativeDir + "/" + filename;
            if (entry.getAttrs().isDir()) {
                if (recursive) {
                    listFiles(channel, basePath, relativePath, true, sink);
                }
                continue;
            }
            sink.next(FileMetadata.builder()
                    .fileName(filename)
                    .fileSize(entry.getAttrs().getSize())
                    .lastModified(entry.getAttrs().getMTime() * 1000L)
                    .relativePath(relativePath)
                    .build());
        }
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
            String remoteFile = remotePath.endsWith("/") ? remotePath + fileMetaData.getRelativePath()
                    : remotePath + "/" + fileMetaData.getRelativePath();
            try (var out = Files.newOutputStream(tempFile)) {
                channel.get(remoteFile, out);
            }
            fileRunContext.setFile(tempFile);
        } catch (Exception e) {
            throw new RuntimeException("SFTP download failed: " + fileMetaData.getRelativePath(), e);
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

            String targetPath = (String) args.getOrDefault("remotePath", "/");
            String relativePath = fileMetaData.getRelativePath();
            if (relativePath != null && !relativePath.isEmpty()) {
                String fullDir = targetPath;
                int lastSlash = relativePath.lastIndexOf('/');
                if (lastSlash > 0) {
                    String subDirs = relativePath.substring(0, lastSlash);
                    fullDir = targetPath.endsWith("/") ? targetPath + subDirs : targetPath + "/" + subDirs;
                    mkdirs(channel, fullDir);
                }
                String uploadPath = targetPath.endsWith("/") ? targetPath + relativePath : targetPath + "/" + relativePath;
                try (var in = Files.newInputStream(fileRunContext.getFile())) {
                    channel.put(in, uploadPath);
                }
            } else {
                String uploadPath = targetPath.endsWith("/") ? targetPath + fileMetaData.getFileName()
                        : targetPath + "/" + fileMetaData.getFileName();
                try (var in = Files.newInputStream(fileRunContext.getFile())) {
                    channel.put(in, uploadPath);
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("SFTP upload failed: " + fileMetaData.getFileName(), e);
        } finally {
            if (channel != null) channel.disconnect();
            if (session != null) session.disconnect();
        }
    }

    private void mkdirs(ChannelSftp channel, String path) {
        try {
            String[] dirs = path.split("/");
            StringBuilder current = new StringBuilder();
            for (String dir : dirs) {
                if (dir.isEmpty()) continue;
                current.append("/").append(dir);
                try {
                    channel.ls(current.toString());
                } catch (Exception e) {
                    channel.mkdir(current.toString());
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to create directory: " + path, e);
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
            session.setPassword(AesUtil.decrypt(password, aesKey));
        }
        session.setConfig("StrictHostKeyChecking", "no");
        session.connect();
        return session;
    }
}