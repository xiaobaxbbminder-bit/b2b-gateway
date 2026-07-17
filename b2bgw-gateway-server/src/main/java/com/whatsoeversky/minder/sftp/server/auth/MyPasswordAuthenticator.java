package com.whatsoeversky.minder.sftp.server.auth;

import com.whatsoeversky.minder.sftp.entity.SftpUser;
import com.whatsoeversky.minder.sftp.service.SftpUserService;
import lombok.extern.slf4j.Slf4j;
import org.apache.sshd.server.auth.AsyncAuthException;
import org.apache.sshd.server.auth.password.PasswordAuthenticator;
import org.apache.sshd.server.auth.password.PasswordChangeRequiredException;
import org.apache.sshd.server.session.ServerSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@Slf4j
public class MyPasswordAuthenticator implements PasswordAuthenticator {

    @Autowired
    private SftpUserService sftpUserService;

    @Override
    public boolean authenticate(String username, String password, ServerSession session) throws PasswordChangeRequiredException, AsyncAuthException {
        Optional<SftpUser> userOpt = sftpUserService.findByUsername(username);
        if (userOpt.isEmpty()) {
            log.warn("Authentication failed: user not found - {}", username);
            return false;
        }

        SftpUser user = userOpt.get();
        if (!user.getEnabled()) {
            log.warn("Authentication failed: user is disabled - {}", username);
            return false;
        }

        if (!sftpUserService.verifyPassword(password, user.getPassword())) {
            log.warn("Authentication failed: invalid password - {}", username);
            return false;
        }

        log.info("Authentication successful: {}", username);
        return true;
    }
}
