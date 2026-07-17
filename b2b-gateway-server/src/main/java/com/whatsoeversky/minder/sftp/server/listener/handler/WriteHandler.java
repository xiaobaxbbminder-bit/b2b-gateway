package com.whatsoeversky.minder.sftp.server.listener.handler;

import org.apache.sshd.server.session.ServerSession;
import org.apache.sshd.sftp.server.FileHandle;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class WriteHandler {
    public void written(ServerSession session,
                        String remoteHandle,
                        FileHandle localHandle,
                        long offset,
                        byte[] data,
                        int dataOffset,
                        int dataLen,
                        Throwable thrown) throws IOException {

    }
}
