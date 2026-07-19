package com.whatsoeversky.minder.sftp.server.listener;

import com.whatsoeversky.minder.sftp.constants.SftpUserTypeConstants;
import com.whatsoeversky.minder.sftp.dto.SftpOperationLogSaveReqDto;
import com.whatsoeversky.minder.sftp.entity.SftpUser;
import com.whatsoeversky.minder.sftp.enums.SftpOperationLogAction;
import com.whatsoeversky.minder.sftp.enums.SftpOperationLogStatus;
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
        SftpUser sftpUser = sftpStorage.removeCurrentUserInfo(SftpSessionUtils.getSessionId(session));
        if (sftpUser != null) {
            try {
                SftpOperationLogSaveReqDto reqDto = new SftpOperationLogSaveReqDto();
                reqDto.setUsername(sftpUser.getUsername());
                reqDto.setClientAddress(SftpSessionUtils.getRemoteAddress(session));
                reqDto.setAction(SftpOperationLogAction.LOGOUT.name());
                reqDto.setStatus(SftpOperationLogStatus.SUCCESS.name());
                reqDto.setDescription("用户登出: " + sftpUser.getUsername());
                sftpOperationLogService.saveOperationLog(reqDto);
            } catch (Exception e) {
                log.error("Failed to save logout log", e);
            }
        }
    }
}
