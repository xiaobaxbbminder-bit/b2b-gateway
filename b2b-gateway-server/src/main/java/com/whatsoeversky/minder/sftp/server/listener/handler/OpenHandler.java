package com.whatsoeversky.minder.sftp.server.listener.handler;

import com.whatsoeversky.minder.sftp.server.enums.SftpOperationLogAction;
import com.whatsoeversky.minder.sftp.server.storage.SftpStorage;
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
        if (localHandle instanceof DirectoryHandle) {
            return;
        }
        // 文件或者文件夹打开时候的回调方法
        Path file = localHandle.getFile();
        String filePath = file.toString();
        FileHandle fileHandle = (FileHandle) localHandle;
        Set<StandardOpenOption> openOptions = fileHandle.getOpenOptions();
        boolean readOption = FileUtils.isReadOption(openOptions);
        String logId;
        if (readOption) {
            String description = String.format(Locale.ROOT, "文件：%s 正在下载", filePath);
            logId = saveOperationLog(session, SftpOperationLogAction.READ_FILE.name(), filePath, FileUtils.getFileSize(file), description);
        } else {
            String description = String.format(Locale.ROOT, "文件：%s 正在上传", filePath);
            logId = saveOperationLog(session, SftpOperationLogAction.WRITE_FILE.name(), filePath, 0L, description);
        }
        FileRunContext fileRunContext = new FileRunContext();
        fileRunContext.setFile(file);
        fileRunContext.setLogId(logId);
        sftpStorage.saveFileRunContext(remoteHandle, fileRunContext);
    }
}
