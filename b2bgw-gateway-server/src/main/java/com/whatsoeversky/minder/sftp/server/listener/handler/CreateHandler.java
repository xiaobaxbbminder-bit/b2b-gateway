package com.whatsoeversky.minder.sftp.server.listener.handler;

import org.apache.sshd.server.session.ServerSession;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;

@Component
public class CreateHandler extends CommonHandler {
    public void created(ServerSession session,
                        Path path,
                        Map<String, ?> attrs,
                        Throwable thrown) throws IOException {

    }
}
