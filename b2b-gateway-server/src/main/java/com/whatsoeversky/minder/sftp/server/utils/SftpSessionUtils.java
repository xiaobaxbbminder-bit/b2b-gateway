package com.whatsoeversky.minder.sftp.server.utils;

import com.whatsoeversky.minder.utils.HostUtils;
import jakarta.annotation.Nullable;
import org.apache.sshd.common.session.Session;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.Base64;

public class SftpSessionUtils {
    public static String getSessionId(Session session) {
        return Base64.getEncoder().encodeToString(session.getSessionId());
    }

    @Nullable
    public static String getRemoteHostname(Session session) {
        SocketAddress remoteAddress = session.getRemoteAddress();
        if (remoteAddress instanceof InetSocketAddress inetSocketAddress) {
            return HostUtils.getHostname(inetSocketAddress);
        }
        return null;
    }


}
