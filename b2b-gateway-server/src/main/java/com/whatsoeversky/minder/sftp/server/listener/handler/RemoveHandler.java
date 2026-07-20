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

@Slf4j
@Component
public class RemoveHandler extends CommonHandler {
    @Resource
    private SftpStorage sftpStorage;

    public void removing(ServerSession session, Path path, boolean isDirectory) throws IOException {
        SftpUser user = sftpStorage.getCurrentUserInfo(SftpSessionUtils.getSessionId(session));
        SftpPermission permissions = user != null ? user.getPermissions() : null;
        if (permissions == null) {
            return;
        }
        if (isDirectory && !permissions.isDeleteFolder()) {
            log.warn("Permission denied: deleteFolder - user: {}, path: {}", session.getUsername(), path);
            throw new IOException("Permission denied: delete folder");
        }
        if (!isDirectory && !permissions.isDeleteFile()) {
            log.warn("Permission denied: deleteFile - user: {}, path: {}", session.getUsername(), path);
            throw new IOException("Permission denied: delete file");
        }
    }

    public void removed(ServerSession session,
                        Path path,
                        boolean isDirectory,
                        Throwable thrown) throws IOException {
        String filePath = path.toString();
        String action = isDirectory ? SftpOperationLogAction.REMOVE_DIRECTORY.name() : SftpOperationLogAction.DELETE_FILE.name();
        String type = isDirectory ? "删除文件夹" : "删除文件";
        if (thrown != null) {
            saveOperationLog(session,
                    action,
                    filePath,
                    0L,
                    String.format(Locale.ROOT, "%s失败: %s %s", type, filePath, thrown.getMessage()),
                    SftpOperationLogStatus.ERROR);
        } else {
            saveOperationLog(session,
                    action,
                    filePath,
                    0L,
                    String.format(Locale.ROOT, "%s成功: %s", type, filePath),
                    SftpOperationLogStatus.COMPLETED);
        }
    }
}