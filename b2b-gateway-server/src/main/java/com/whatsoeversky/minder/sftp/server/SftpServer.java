package com.whatsoeversky.minder.sftp.server;

import com.whatsoeversky.minder.sftp.server.auth.MyPasswordAuthenticator;
import com.whatsoeversky.minder.sftp.server.auth.MyPublicKeyAuthenticator;
import com.whatsoeversky.minder.sftp.server.fs.MyFileSystemFactory;
import com.whatsoeversky.minder.sftp.server.hostkey.MyKeyPairProvider;
import com.whatsoeversky.minder.sftp.server.listener.MySftpEventListener;
import com.whatsoeversky.minder.sftp.server.listener.MySftpSessionListener;
import com.whatsoeversky.minder.sftp.service.SftpUserService;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.sshd.server.SshServer;
import org.apache.sshd.sftp.server.SftpSubsystemFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Collections;

@Component
@Slf4j
public class SftpServer {
    @Resource
    private MyFileSystemFactory myFileSystemFactory;

    @Resource
    private MySftpEventListener mySftpEventListener;

    @Resource
    private MyPasswordAuthenticator myPasswordAuthenticator;

    @Resource
    private MyPublicKeyAuthenticator myPublicKeyAuthenticator;

    @Resource
    private MyKeyPairProvider myKeyPairProvider;

    @Resource
    private MySftpSessionListener mySftpSessionListener;

    @Resource
    private SftpUserService sftpUserService;

    @Value("${sftp.server-port}")
    private int serverPort;

    private SshServer sshServer;

    @PostConstruct
    public void init() throws IOException {
        sshServer = SshServer.setUpDefaultServer();

        // 主机秘钥配置
        sshServer.setKeyPairProvider(myKeyPairProvider);

        // sftp子系统
        SftpSubsystemFactory sftpSubsystemFactory = new SftpSubsystemFactory();
        sftpSubsystemFactory.addSftpEventListener(mySftpEventListener);
        sshServer.setSubsystemFactories(Collections.singletonList(sftpSubsystemFactory));

        // 文件系统
        sshServer.setFileSystemFactory(myFileSystemFactory);

        // 认证
        sshServer.setPasswordAuthenticator(myPasswordAuthenticator);
        sshServer.setPublickeyAuthenticator(myPublicKeyAuthenticator);

        // 会话监听器
        sshServer.addSessionListener(mySftpSessionListener);

        sshServer.setPort(serverPort);
        sshServer.start();
        log.info("SFTP server started on port {}", serverPort);
    }

    @PreDestroy
    public void destroy() throws IOException {
        sshServer.close();
    }
}
