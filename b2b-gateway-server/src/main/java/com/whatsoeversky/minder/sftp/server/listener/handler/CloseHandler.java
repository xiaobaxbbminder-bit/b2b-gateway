package com.whatsoeversky.minder.sftp.server.listener.handler;

import com.whatsoeversky.minder.sftp.constants.SftpExecuteNodeConstants;
import com.whatsoeversky.minder.sftp.entity.SftpServiceConfig;
import com.whatsoeversky.minder.sftp.entity.SftpUser;
import com.whatsoeversky.minder.sftp.repository.SftpServiceConfigRepository;
import com.whatsoeversky.minder.sftp.enums.SftpOperationLogAction;
import com.whatsoeversky.minder.sftp.enums.SftpOperationLogStatus;
import com.whatsoeversky.minder.sftp.server.listener.handler.flow.PluginChain;
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
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;

@Component
@Slf4j
public class CloseHandler extends CommonHandler {
    @Resource
    private SftpStorage sftpStorage;
    @Resource
    private SftpServiceConfigRepository sftpServiceConfigRepository;
    @Resource
    private PluginChain pluginChain;

    public void closed(ServerSession session,
                       String remoteHandle,
                       Handle localHandle,
                       Throwable thrown) throws IOException {
        if (thrown != null) {
            log.error("error closed", thrown);
        }
        if (localHandle instanceof DirectoryHandle) {
            return;
        }
        FileHandle fileHandle = (FileHandle) localHandle;
        Set<StandardOpenOption> openOptions = fileHandle.getOpenOptions();
        boolean readOption = FileUtils.isReadOption(openOptions);
        if (readOption) {
            handleFileDownload(session, remoteHandle, localHandle, thrown);
        } else {
            handleFileUpload(session, remoteHandle, localHandle, thrown);
        }
    }

    private void handleFileUpload(ServerSession session,
                                  String remoteHandle,
                                  Handle localHandle,
                                  Throwable thrown) {
        try {
            FileRunContext fileRunContext = sftpStorage.getFileRunContext(remoteHandle);
            if (Objects.isNull(fileRunContext)) {
                log.error("error get file run context for upload");
            }
            String logId = fileRunContext.getLogId();
            if (!StringUtils.hasLength(logId)) {
                log.error("error get log id for upload");
            }
            if (thrown != null) {
                updateOperationLog(logId, SftpOperationLogAction.WRITE_FILE, SftpOperationLogStatus.ERROR, thrown.getMessage());
            } else {
                Path file = localHandle.getFile();
                String description = String.format(Locale.ROOT, "文件：%s 上传成功", file.toString());
                updateOperationLog(logId, SftpOperationLogAction.WRITE_FILE, SftpOperationLogStatus.SUCCESS, description);
                SftpUser currentUser = sftpStorage.getCurrentUserInfo(getSessionId(session));
                List<SftpServiceConfig> sftpServiceConfigList = sftpServiceConfigRepository.findByUserId(currentUser.getId());
                if (!CollectionUtils.isEmpty(sftpServiceConfigList)) {
                    pluginChain.doPluginChain(fileRunContext, sftpServiceConfigList, SftpExecuteNodeConstants.AFTER_UPLOAD);
                }
            }
        } finally {
            sftpStorage.clearRemoteHandle(remoteHandle);
        }
    }

    private void handleFileDownload(ServerSession session,
                                    String remoteHandle,
                                    Handle localHandle,
                                    Throwable thrown) {
        try {
            FileRunContext fileRunContext = sftpStorage.getFileRunContext(remoteHandle);
            if (Objects.isNull(fileRunContext)) {
                log.error("error get file run context for download");
                return;
            }
            String logId = fileRunContext.getLogId();
            if (!StringUtils.hasLength(logId)) {
                log.error("error get log id for download");
                return;
            }
            if (thrown != null) {
                updateOperationLog(logId, SftpOperationLogAction.READ_FILE, SftpOperationLogStatus.ERROR, thrown.getMessage());
            } else {
                Path file = localHandle.getFile();
                String description = String.format(Locale.ROOT, "文件：%s 下载成功", file.toString());
                SftpUser currentUser = sftpStorage.getCurrentUserInfo(getSessionId(session));
                List<SftpServiceConfig> sftpServiceConfigList = sftpServiceConfigRepository.findByUserId(currentUser.getId());
                if (!CollectionUtils.isEmpty(sftpServiceConfigList)) {
                    pluginChain.doPluginChain(fileRunContext, sftpServiceConfigList, SftpExecuteNodeConstants.AFTER_DOWNLOAD);
                }
                updateOperationLog(logId, SftpOperationLogAction.READ_FILE, SftpOperationLogStatus.SUCCESS, description);
            }
        } finally {
            sftpStorage.clearRemoteHandle(remoteHandle);
        }

    }

}
