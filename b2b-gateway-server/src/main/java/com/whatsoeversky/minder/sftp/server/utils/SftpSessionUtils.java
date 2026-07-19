package com.whatsoeversky.minder.sftp.server.utils;

import com.whatsoeversky.minder.utils.HostUtils;
import jakarta.annotation.Nullable;
import org.apache.sshd.common.session.Session;
import org.apache.sshd.server.session.ServerSession;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.Base64;

public class SftpSessionUtils {
    public static String getSessionId(Session session) {
        return Base64.getEncoder().encodeToString(session.getSessionId());
    }

    public static String getClientAddress(ServerSession session) {
        try {
            return session.getClientAddress().toString();
        } catch (Exception e) {
            return "unknown";
        }
    }


}
