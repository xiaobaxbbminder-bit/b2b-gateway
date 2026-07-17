package com.whatsoeversky.minder.sftp.server.listener.handler;

import org.apache.sshd.server.session.ServerSession;
import org.apache.sshd.sftp.server.FileHandle;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class ReadHandler extends CommonHandler {


    public void read(ServerSession session,
                     String remoteHandle,
                     FileHandle localHandle,
                     long offset,
                     byte[] data,
                     int dataOffset,
                     int dataLen,
                     int readLen, Throwable thrown) throws IOException {

    }
}
