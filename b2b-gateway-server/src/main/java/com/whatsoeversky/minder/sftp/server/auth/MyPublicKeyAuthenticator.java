package com.whatsoeversky.minder.sftp.server.auth;

import org.apache.sshd.server.auth.AsyncAuthException;
import org.apache.sshd.server.auth.pubkey.PublickeyAuthenticator;
import org.apache.sshd.server.session.ServerSession;
import org.springframework.stereotype.Component;

import java.security.PublicKey;

@Component
public class MyPublicKeyAuthenticator implements PublickeyAuthenticator {
    @Override
    public boolean authenticate(String username, PublicKey key, ServerSession session) throws AsyncAuthException {
        return false;
    }
}
