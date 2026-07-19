package com.whatsoeversky.minder.sftp.server.storage;

import com.whatsoeversky.minder.sftp.entity.SftpUser;
import com.whatsoeversky.minder.sftp.support.FileRunContext;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class SftpStorage {
    private final Map<String, FileRunContext> fileRunContextMap = new ConcurrentHashMap<>();

    private final Map<String, SftpUser> sessionUserInfo = new ConcurrentHashMap<>();

    public FileRunContext getFileRunContext(String remoteHandle) {
        return fileRunContextMap.get(remoteHandle);
    }

    public void saveFileRunContext(String remoteHandle, FileRunContext fileRunContext) {
        fileRunContextMap.put(remoteHandle, fileRunContext);
    }

    public void clearRemoteHandle(String remoteHandle) {
        fileRunContextMap.remove(remoteHandle);
    }

    public SftpUser getCurrentUserInfo(String sessionId) {
        return sessionUserInfo.get(sessionId);
    }

    public void setCurrentUserInfo(String sessionId, SftpUser sftpUser) {
        sessionUserInfo.put(sessionId, sftpUser);
    }

    public SftpUser removeCurrentUserInfo(String sessionId) {
        return sessionUserInfo.remove(sessionId);
    }
}
