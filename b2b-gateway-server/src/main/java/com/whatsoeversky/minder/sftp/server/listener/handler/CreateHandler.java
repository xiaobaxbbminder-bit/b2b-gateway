package com.whatsoeversky.minder.sftp.server.listener.handler;

import com.whatsoeversky.minder.sftp.entity.SftpPermission;
import com.whatsoeversky.minder.sftp.entity.SftpUser;
import com.whatsoeversky.minder.sftp.enums.SftpOperationLogAction;
import com.whatsoeversky.minder.sftp.enums.SftpOperationLogStatus;
import com.whatsoeversky.minder.sftp.server.storage.SftpStorage;
import com.whatsoeversky.minder.sftp.server.utils.SftpSessionUtils;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.sshd.server.session.ServerSession;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Locale;
import java.util.Map;

@Slf4j
@Component
public class CreateHandler extends CommonHandler {
    @Resource
    private SftpStorage sftpStorage;

    public void creating(ServerSession session, Path path, Map<String, ?> attrs) throws IOException {
        SftpUser user = sftpStorage.getCurrentUserInfo(SftpSessionUtils.getSessionId(session));
        SftpPermission permissions = user != null ? user.getPermissions() : null;
        if (permissions != null && !permissions.isCreateFolder()) {
            log.warn("Permission denied: createFolder - user: {}, path: {}", session.getUsername(), path);
            throw new IOException("Permission denied: create folder");
        }
    }

    public void created(ServerSession session,
                        Path path,
                        Map<String, ?> attrs,
                        Throwable thrown) throws IOException {
        String filePath = path.toString();
        if (thrown != null) {
            saveOperationLog(session,
                    SftpOperationLogAction.CREATE_DIRECTORY.name(),
                    filePath,
                    0L,
                    String.format(Locale.ROOT, "创建文件夹失败: %s %s", filePath, thrown.getMessage()),
                    SftpOperationLogStatus.ERROR);
        } else {
            saveOperationLog(session,
                    SftpOperationLogAction.CREATE_DIRECTORY.name(),
                    filePath,
                    0L,
                    String.format(Locale.ROOT, "创建文件夹成功: %s", filePath),
                    SftpOperationLogStatus.COMPLETED);
        }
    }


}