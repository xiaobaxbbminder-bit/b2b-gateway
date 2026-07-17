package com.whatsoeversky.minder.sftp.server.hostkey;

import com.whatsoeversky.minder.utils.HostUtils;
import com.whatsoeversky.minder.utils.RSAUtils;
import com.whatsoeversky.minder.utils.SSHDKeyUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.sshd.common.keyprovider.KeyPairProvider;
import org.apache.sshd.common.session.SessionContext;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.List;

@Component
@Slf4j
public class MyKeyPairProvider implements KeyPairProvider {
    @Value("${sftp.rsa-host-key}")
    private String rsaHostKey;

    @Value("${sftp.ecdsa-host-key:}")
    private String ecdsaHostKey;

    @Value("${sftp.ed25519-host-key:}")
    private String ed25519HostKey;

    @Override
    public Iterable<KeyPair> loadKeys(SessionContext session) {
        String username = session.getUsername();
        SocketAddress remoteSocketAddress = session.getRemoteAddress();
        String remoteAddress = "";
        int port = -1;
        if (remoteSocketAddress instanceof InetSocketAddress inetSocketAddress) {
            remoteAddress = HostUtils.getHostname(inetSocketAddress);
            port = HostUtils.getPort(inetSocketAddress);
        }
        SocketAddress localSocketAddress = session.getLocalAddress();
        String localAddress = "";
        int localPort = -1;
        if (localSocketAddress instanceof InetSocketAddress inetSocketAddress) {
            localAddress = HostUtils.getHostname(inetSocketAddress);
            localPort = HostUtils.getPort(inetSocketAddress);
        }

        log.info("username: {}, remote address: {}, remote port: {}, local address: {}, local port: {}",
                username, remoteAddress, port, localAddress, localPort);

        List<KeyPair> keys = new ArrayList<>();

        PrivateKey privateKey = RSAUtils.loadPrivateKey(rsaHostKey);
        PublicKey publicKey = RSAUtils.getPublicKeyFromPrivateKey(privateKey);
        keys.add(new KeyPair(publicKey, privateKey));

        if (ecdsaHostKey != null && !ecdsaHostKey.isBlank()) {
            KeyPair ecdsaKeyPair = SSHDKeyUtils.loadOpenSshKeyPair(ecdsaHostKey, StringUtils.EMPTY);
            keys.add(ecdsaKeyPair);
        }

        if (ed25519HostKey != null && !ed25519HostKey.isBlank()) {
            KeyPair ed25519KeyPair = SSHDKeyUtils.loadOpenSshKeyPair(ed25519HostKey,StringUtils.EMPTY);
            keys.add(ed25519KeyPair);
        }

        return keys;
    }
}
