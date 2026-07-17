package com.whatsoeversky.minder.sftp.server.listener;

import com.whatsoeversky.minder.sftp.constants.SftpUserTypeConstants;
import com.whatsoeversky.minder.sftp.entity.SftpUser;
import com.whatsoeversky.minder.sftp.repository.SftpUserRepository;
import com.whatsoeversky.minder.sftp.server.storage.SftpStorage;
import com.whatsoeversky.minder.sftp.server.utils.SftpSessionUtils;
import jakarta.annotation.Resource;
import org.apache.sshd.common.session.Session;
import org.apache.sshd.common.session.SessionListener;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class MySftpSessionListener implements SessionListener {
    @Resource
    private SftpStorage sftpStorage;
    @Resource
    private SftpUserRepository sftpUserRepository;

    @Override
    public void sessionEvent(Session session, Event event) {
        if (event == Event.Authenticated) {
            Optional<SftpUser> sftpUserOptional = sftpUserRepository.findByUsernameAndUserType(session.getUsername(), SftpUserTypeConstants.USER_TYPE_SERVER);
            SftpUser sftpUser = sftpUserOptional.orElseThrow(() -> new RuntimeException("error find user"));
            sftpStorage.setCurrentUserInfo(SftpSessionUtils.getSessionId(session), sftpUser);
        }
    }

    @Override
    public void sessionClosed(Session session) {
        sftpStorage.removeCurrentUserInfo(SftpSessionUtils.getSessionId(session));
    }
}
