package com.whatsoeversky.minder.sftp.server.listener;

import com.whatsoeversky.minder.sftp.constants.SftpUserTypeConstants;
import com.whatsoeversky.minder.sftp.entity.SftpUser;
import com.whatsoeversky.minder.sftp.repository.SftpUserRepository;
import com.whatsoeversky.minder.sftp.server.storage.SftpStorage;
import com.whatsoeversky.minder.sftp.server.utils.SftpSessionUtils;
import com.whatsoeversky.minder.sftp.service.SftpOperationLogService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.sshd.common.session.Session;
import org.apache.sshd.common.session.SessionListener;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@Slf4j
public class MySftpSessionListener implements SessionListener {
    @Resource
    private SftpStorage sftpStorage;
    @Resource
    private SftpUserRepository sftpUserRepository;
    @Resource
    private SftpOperationLogService sftpOperationLogService;

    @Override
    public void sessionEvent(Session session, Event event) {
        if (event == Event.KexCompleted) {
            log.info("kex completed, username: {}, remote hostname: {}", session.getUsername(), SftpSessionUtils.getRemoteAddress(session));
        } else if (event == Event.KeyEstablished) {
            log.info("key established, username: {}, remote hostname: {}", session.getUsername(), SftpSessionUtils.getRemoteAddress(session));
        } else if (event == Event.Authenticated) {
            log.info("authenticated, username: {}, remote hostname: {}", session.getUsername(), SftpSessionUtils.getRemoteAddress(session));
            Optional<SftpUser> sftpUserOptional = sftpUserRepository.findByUsernameAndUserType(session.getUsername(), SftpUserTypeConstants.USER_TYPE_SERVER);
            SftpUser sftpUser = sftpUserOptional.orElseThrow(() -> new RuntimeException("error find user"));
            sftpStorage.setCurrentUserInfo(SftpSessionUtils.getSessionId(session), sftpUser);
        }
    }

    @Override
    public void sessionException(Session session, Throwable t) {
        log.error("session exception, username: {}, remote hostname: {}", session.getUsername(), SftpSessionUtils.getRemoteAddress(session), t);
    }

    @Override
    public void sessionClosed(Session session) {
        log.info("session closed, username: {}, remote hostname: {}", session.getUsername(), SftpSessionUtils.getRemoteAddress(session));
        sftpStorage.removeCurrentUserInfo(SftpSessionUtils.getSessionId(session));
    }
}
