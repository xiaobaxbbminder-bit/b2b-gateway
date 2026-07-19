package com.whatsoeversky.minder.sftp.server.auth;

import com.whatsoeversky.minder.sftp.constants.SftpUserTypeConstants;
import com.whatsoeversky.minder.sftp.dto.SftpOperationLogSaveReqDto;
import com.whatsoeversky.minder.sftp.entity.SftpUser;
import com.whatsoeversky.minder.sftp.repository.SftpUserRepository;
import com.whatsoeversky.minder.sftp.server.enums.SftpOperationLogAction;
import com.whatsoeversky.minder.sftp.server.enums.SftpOperationLogStatus;
import com.whatsoeversky.minder.sftp.server.utils.SftpSessionUtils;
import com.whatsoeversky.minder.sftp.service.SftpOperationLogService;
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

    @Resource
    private SftpOperationLogService sftpOperationLogService;

    @Override
    public boolean authenticate(String username, PublicKey key, ServerSession session) throws AsyncAuthException {
        String clientAddress = SftpSessionUtils.getClientAddress(session);

        Optional<SftpUser> userOpt = sftpUserRepository.findByUsernameAndUserType(username, SftpUserTypeConstants.USER_TYPE_SERVER);
        if (userOpt.isEmpty()) {
            log.warn("Public key authentication failed: user not found - {}", username);
            saveAuthLog(username, clientAddress, SftpOperationLogStatus.ERROR, "用户不存在: " + username);
            return false;
        }

        SftpUser user = userOpt.get();
        if (!user.getEnabled()) {
            log.warn("Public key authentication failed: user is disabled - {}", username);
            saveAuthLog(username, clientAddress, SftpOperationLogStatus.ERROR, "用户已禁用: " + username);
            return false;
        }

        String storedPublicKey = user.getPublicKey();
        if (storedPublicKey == null || storedPublicKey.isBlank()) {
            log.warn("Public key authentication failed: user has no public key - {}", username);
            saveAuthLog(username, clientAddress, SftpOperationLogStatus.ERROR, "用户未配置公钥: " + username);
            return false;
        }

        try {
            PublicKey expectedKey = SSHDKeyUtils.loadPublicKey(storedPublicKey.trim());
            if (expectedKey.equals(key)) {
                log.info("Public key authentication successful: {}", username);
                saveAuthLog(username, clientAddress, SftpOperationLogStatus.SUCCESS, "公钥认证成功: " + username);
                return true;
            }
        } catch (Exception e) {
            log.error("Public key authentication failed: error loading stored public key - {}", username, e);
            saveAuthLog(username, clientAddress, SftpOperationLogStatus.ERROR, "公钥解析失败: " + username);
            return false;
        }

        log.warn("Public key authentication failed: key mismatch - {}", username);
        saveAuthLog(username, clientAddress, SftpOperationLogStatus.ERROR, "公钥不匹配: " + username);
        return false;
    }

    private void saveAuthLog(String username, String clientAddress, SftpOperationLogStatus status, String description) {
        try {
            SftpOperationLogSaveReqDto reqDto = new SftpOperationLogSaveReqDto();
            reqDto.setUsername(username);
            reqDto.setClientAddress(clientAddress);
            reqDto.setAction(SftpOperationLogAction.LOGIN.name());
            reqDto.setStatus(status.name());
            reqDto.setDescription(description);
            sftpOperationLogService.saveOperationLog(reqDto);
        } catch (Exception e) {
            log.error("Failed to save auth log", e);
        }
    }
}