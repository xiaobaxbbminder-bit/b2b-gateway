package com.whatsoeversky.minder.sftp.server.utils;

import org.apache.sshd.common.session.Session;

import java.util.Base64;

public class SftpSessionUtils {
    public static String getSessionId(Session session) {
        return Base64.getEncoder().encodeToString(session.getSessionId());
    }
}
