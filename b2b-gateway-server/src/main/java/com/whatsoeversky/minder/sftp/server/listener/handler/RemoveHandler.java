package com.whatsoeversky.minder.sftp.server.listener.handler;

import org.apache.sshd.server.session.ServerSession;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Path;

@Component
public class RemoveHandler extends CommonHandler {
    public void removed(ServerSession session,
                        Path path,
                        boolean isDirectory,
                        Throwable thrown) throws IOException {

    }
}
