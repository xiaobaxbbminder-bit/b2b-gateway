package com.whatsoeversky.minder.sftp.server.listener.handler;

import com.whatsoeversky.minder.sftp.server.enums.SftpOperationLogAction;
import com.whatsoeversky.minder.sftp.server.enums.SftpOperationLogStatus;
import com.whatsoeversky.minder.sftp.service.SftpOperationLogService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.sshd.server.session.ServerSession;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
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
        String clientAddress = getClientAddress(session);
        String username = session.getUsername();
        return sftpOperationLogService.saveOperationLog(username, clientAddress, action, filePath, fileSize, description);
    }

    protected void updateOperationLog(String logId,
                                      SftpOperationLogAction action,
                                      SftpOperationLogStatus status,
                                      String description) {
        sftpOperationLogService.updateOperationLog(logId, action.name(), status.name(), description);
    }

    protected String getClientAddress(ServerSession session) {
        SocketAddress remoteAddress = session.getRemoteAddress();
        if (remoteAddress instanceof InetSocketAddress inetetSocketAddress) {
            return inetetSocketAddress.getAddress().getHostAddress() + ":" + inetetSocketAddress.getPort();
        }
        return remoteAddress.toString();
    }

    protected String getSessionId(ServerSession session) {
        return Base64.getEncoder().encodeToString(session.getSessionId());
    }
}
