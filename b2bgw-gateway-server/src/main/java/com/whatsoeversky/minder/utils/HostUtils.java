package com.whatsoeversky.minder.utils;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;

public class HostUtils {
    public static String getLocalHostname() {
        try {
            return InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException e) {
            throw new RuntimeException(e);
        }
    }

    public static String getHostname(InetSocketAddress inetSocketAddress) {
        return inetSocketAddress.getHostName();
    }

    public static int getPort(InetSocketAddress inetSocketAddress) {
        return inetSocketAddress.getPort();
    }
}
