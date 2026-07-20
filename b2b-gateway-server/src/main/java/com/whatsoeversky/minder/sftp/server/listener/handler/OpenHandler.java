package com.whatsoeversky.minder.sftp.server.listener.handler;

import com.whatsoeversky.minder.sftp.entity.SftpPermission;
import com.whatsoeversky.minder.sftp.entity.SftpUser;
import com.whatsoeversky.minder.sftp.enums.SftpOperationLogAction;
import com.whatsoeversky.minder.sftp.enums.SftpOperationLogStatus;
import com.whatsoeversky.minder.sftp.server.storage.SftpStorage;
import com.whatsoeversky.minder.sftp.server.utils.SftpSessionUtils;
import com.whatsoeversky.minder.sftp.support.FileRunContext;
import com.whatsoeversky.minder.utils.FileUtils;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.sshd.server.session.ServerSession;
import org.apache.sshd.sftp.server.DirectoryHandle;
import org.apache.sshd.sftp.server.FileHandle;
import org.apache.sshd.sftp.server.Handle;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Locale;
import java.util.Set;

@Slf4j
@Component
public class OpenHandler extends CommonHandler {
    @Resource
    private SftpStorage sftpStorage;

    public void opening(ServerSession session,
                        String remoteHandle,
                        Handle localHandle) throws IOException {
        SftpUser user = sftpStorage.getCurrentUserInfo(SftpSessionUtils.getSessionId(session));
        SftpPermission permissions = user != null ? user.getPermissions() : null;

        if (localHandle instanceof DirectoryHandle) {
            if (permissions != null && !permissions.isListDir()) {
                log.warn("Permission denied: listDir - user: {}, path: {}", session.getUsername(), localHandle.getFile());
                throw new IOException("Permission denied: list directory");
            }
            return;
        }

        Path file = localHandle.getFile();
        String filePath = file.toString();
        FileHandle fileHandle = (FileHandle) localHandle;
        Set<StandardOpenOption> openOptions = fileHandle.getOpenOptions();
        boolean readOption = FileUtils.isReadOption(openOptions);

        if (readOption) {
            if (permissions != null && !permissions.isRead()) {
                log.warn("Permission denied: read - user: {}, path: {}", session.getUsername(), filePath);
                throw new IOException("Permission denied: read file");
            }
            String description = String.format(Locale.ROOT, "文件：%s 正在下载", filePath);
            String logId = saveOperationLog(session,
                    SftpOperationLogAction.READ_FILE.name(),
                    filePath,
                    FileUtils.getFileSize(file),
                    description,
                    SftpOperationLogStatus.PENDING);
            saveRunContext(remoteHandle, file, logId);
        } else {
            if (permissions != null && !permissions.isWrite()) {
                log.warn("Permission denied: write - user: {}, path: {}", session.getUsername(), filePath);
                throw new IOException("Permission denied: write file");
            }
            String description = String.format(Locale.ROOT, "文件：%s 正在上传", filePath);
            String logId = saveOperationLog(session,
                    SftpOperationLogAction.WRITE_FILE.name(),
                    filePath,
                    0L,
                    description,
                    SftpOperationLogStatus.PENDING);
            saveRunContext(remoteHandle, file, logId);
        }
    }

    private void saveRunContext(String remoteHandle, Path file, String logId) {
        FileRunContext fileRunContext = new FileRunContext();
        fileRunContext.setFile(file);
        fileRunContext.setLogId(logId);
        sftpStorage.saveFileRunContext(remoteHandle, fileRunContext);
    }
}