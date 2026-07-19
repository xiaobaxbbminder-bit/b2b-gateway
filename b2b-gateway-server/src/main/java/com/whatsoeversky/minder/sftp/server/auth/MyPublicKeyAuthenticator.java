package com.whatsoeversky.minder.sftp.server.auth;

import com.whatsoeversky.minder.sftp.constants.SftpUserTypeConstants;
import com.whatsoeversky.minder.sftp.entity.SftpUser;
import com.whatsoeversky.minder.sftp.repository.SftpUserRepository;
import com.whatsoeversky.minder.utils.SSHDKeyUtils;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.sshd.server.auth.AsyncAuthException;
import org.apache.sshd.server.auth.pubkey.PublickeyAuthenticator;
import org.apache.sshd.server.session.ServerSession;
import org.springframework.stereotype.Component;

import java.security.PublicKey;
import java.util.Optional;

@Component
@Slf4j
public class MyPublicKeyAuthenticator implements PublickeyAuthenticator {
    @Resource
    private SftpUserRepository sftpUserRepository;

    @Override
    public boolean authenticate(String username, PublicKey key, ServerSession session) throws AsyncAuthException {
        Optional<SftpUser> userOpt = sftpUserRepository.findByUsernameAndUserType(username, SftpUserTypeConstants.USER_TYPE_SERVER);
        if (userOpt.isEmpty()) {
            log.warn("Public key authentication failed: user not found - {}", username);
            return false;
        }

        SftpUser user = userOpt.get();
        if (!user.getEnabled()) {
            log.warn("Public key authentication failed: user is disabled - {}", username);
            return false;
        }

        String storedPublicKey = user.getPublicKey();
        if (storedPublicKey == null || storedPublicKey.isBlank()) {
            log.warn("Public key authentication failed: user has no public key - {}", username);
            return false;
        }

        try {
            PublicKey expectedKey = SSHDKeyUtils.loadPublicKey(storedPublicKey.trim());
            if (expectedKey.equals(key)) {
                log.info("Public key authentication successful: {}", username);
                return true;
            }
        } catch (Exception e) {
            log.error("Public key authentication failed: error loading stored public key - {}", username, e);
            return false;
        }

        log.warn("Public key authentication failed: key mismatch - {}", username);
        return false;
    }
}
