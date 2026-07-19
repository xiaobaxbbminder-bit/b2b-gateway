package com.whatsoeversky.minder.sftp.server.auth;

import com.whatsoeversky.minder.sftp.constants.SftpUserTypeConstants;
import com.whatsoeversky.minder.sftp.dto.SftpOperationLogSaveReqDto;
import com.whatsoeversky.minder.sftp.entity.SftpUser;
import com.whatsoeversky.minder.sftp.repository.SftpUserRepository;
import com.whatsoeversky.minder.sftp.server.enums.SftpOperationLogAction;
import com.whatsoeversky.minder.sftp.server.enums.SftpOperationLogStatus;
import com.whatsoeversky.minder.sftp.server.utils.SftpSessionUtils;
import com.whatsoeversky.minder.sftp.service.SftpOperationLogService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.sshd.server.auth.AsyncAuthException;
import org.apache.sshd.server.auth.password.PasswordAuthenticator;
import org.apache.sshd.server.auth.password.PasswordChangeRequiredException;
import org.apache.sshd.server.session.ServerSession;
import org.mindrot.jbcrypt.BCrypt;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@Slf4j
public class MyPasswordAuthenticator implements PasswordAuthenticator {

    @Resource
    private SftpUserRepository sftpUserRepository;

    @Resource
    private SftpOperationLogService sftpOperationLogService;

    @Override
    public boolean authenticate(String username, String password, ServerSession session) throws PasswordChangeRequiredException, AsyncAuthException {
        String clientAddress = SftpSessionUtils.getClientAddress(session);
        Optional<SftpUser> userOpt = sftpUserRepository.findByUsernameAndUserType(username, SftpUserTypeConstants.USER_TYPE_SERVER);
        if (userOpt.isEmpty()) {
            log.warn("Password authentication failed: user not found - {}", username);
            saveAuthLog(username, clientAddress, SftpOperationLogStatus.ERROR, "用户不存在: " + username);
            return false;
        }

        SftpUser user = userOpt.get();
        if (!user.getEnabled()) {
            log.warn("Password authentication failed: user is disabled - {}", username);
            saveAuthLog(username, clientAddress, SftpOperationLogStatus.ERROR, "用户已禁用: " + username);
            return false;
        }

        if (user.getPasswordLogin() != null && !user.getPasswordLogin()) {
            log.warn("Password authentication failed: password login is disabled - {}", username);
            saveAuthLog(username, clientAddress, SftpOperationLogStatus.ERROR, "密码登录已禁用: " + username);
            return false;
        }

        if (!verifyPassword(password, user.getPassword())) {
            log.warn("Password authentication failed: invalid password - {}", username);
            saveAuthLog(username, clientAddress, SftpOperationLogStatus.ERROR, "密码错误: " + username);
            return false;
        }

        log.info("Password authentication successful: {}", username);
        saveAuthLog(username, clientAddress, SftpOperationLogStatus.SUCCESS, "密码认证成功: " + username);
        return true;
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



    private boolean verifyPassword(String rawPassword, String hashedPassword) {
        return BCrypt.checkpw(rawPassword, hashedPassword);
    }

}