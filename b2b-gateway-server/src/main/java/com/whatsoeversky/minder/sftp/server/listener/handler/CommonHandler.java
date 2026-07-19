package com.whatsoeversky.minder.sftp.server.listener.handler;

import com.whatsoeversky.minder.sftp.dto.SftpOperationLogSaveReqDto;
import com.whatsoeversky.minder.sftp.enums.SftpOperationLogAction;
import com.whatsoeversky.minder.sftp.enums.SftpOperationLogStatus;
import com.whatsoeversky.minder.sftp.server.utils.SftpSessionUtils;
import com.whatsoeversky.minder.sftp.service.SftpOperationLogService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.sshd.server.session.ServerSession;

import java.util.Base64;

@Slf4j
public class CommonHandler {

    @Resource
    private SftpOperationLogService sftpOperationLogService;

    protected String saveOperationLog(ServerSession session,
                                      String action,
                                      String filePath,
                                      Long fileSize,
                                      String description) {
        String clientAddress = SftpSessionUtils.getClientAddress(session);
        String username = session.getUsername();
        SftpOperationLogSaveReqDto reqDto = new SftpOperationLogSaveReqDto();
        reqDto.setUsername(username);
        reqDto.setClientAddress(clientAddress);
        reqDto.setFilePath(filePath);
        reqDto.setFileSize(fileSize);
        reqDto.setAction(action);
        reqDto.setDescription(description);
        return sftpOperationLogService.saveOperationLog(reqDto);
    }

    protected void updateOperationLog(String logId,
                                      SftpOperationLogAction action,
                                      SftpOperationLogStatus status,
                                      String description) {
        sftpOperationLogService.updateOperationLog(logId, action.name(), status.name(), description);
    }


    protected String getSessionId(ServerSession session) {
        return Base64.getEncoder().encodeToString(session.getSessionId());
    }
}
