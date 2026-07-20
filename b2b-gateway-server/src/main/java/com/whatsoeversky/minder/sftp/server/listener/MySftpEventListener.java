package com.whatsoeversky.minder.sftp.server.listener;

import com.whatsoeversky.minder.sftp.server.listener.handler.*;
import com.whatsoeversky.minder.sftp.server.support.HandleState;
import lombok.extern.slf4j.Slf4j;
import org.apache.sshd.server.session.ServerSession;
import org.apache.sshd.sftp.server.FileHandle;
import org.apache.sshd.sftp.server.Handle;
import org.apache.sshd.sftp.server.SftpEventListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
@Slf4j
public class MySftpEventListener implements SftpEventListener {

    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    private CreateHandler createHandler;

    @Autowired
    private RemoveHandler removeHandler;

    @Autowired
    private OpenHandler openHandler;

    @Autowired
    private ReadHandler readHandler;

    @Autowired
    private WriteHandler writeHandler;

    @Autowired
    private CloseHandler closeHandler;

    private final Map<String, HandleState> handleStates = new ConcurrentHashMap<>();

    @Override
    public void opening(ServerSession session,
                        String remoteHandle,
                        Handle localHandle) throws IOException {
        openHandler.opening(session, remoteHandle, localHandle);
    }

    @Override
    public void written(ServerSession session,
                        String remoteHandle,
                        FileHandle localHandle,
                        long offset,
                        byte[] data,
                        int dataOffset,
                        int dataLen,
                        Throwable thrown) throws IOException {
        writeHandler.written(session,
                remoteHandle,
                localHandle,
                offset,
                data,
                dataOffset,
                dataLen,
                thrown
        );
    }

    @Override
    public void read(ServerSession session,
                     String remoteHandle,
                     FileHandle localHandle,
                     long offset,
                     byte[] data,
                     int dataOffset,
                     int dataLen,
                     int readLen,
                     Throwable thrown) throws IOException {
        readHandler.read(session,
                remoteHandle,
                localHandle,
                offset,
                data,
                dataOffset,
                dataLen,
                readLen,
                thrown
        );
    }

    @Override
    public void closed(ServerSession session,
                       String remoteHandle,
                       Handle localHandle,
                       Throwable thrown) throws IOException {
        closeHandler.closed(session, remoteHandle, localHandle, thrown);
    }

    @Override
    public void removing(ServerSession session, Path path, boolean isDirectory) throws IOException {
        removeHandler.removing(session, path, isDirectory);
    }

    @Override
    public void removed(ServerSession session,
                        Path path,
                        boolean isDirectory,
                        Throwable thrown) throws IOException {
        removeHandler.removed(session, path, isDirectory, thrown);
    }

    @Override
    public void creating(ServerSession session, Path path, Map<String, ?> attrs) throws IOException {
        createHandler.creating(session, path, attrs);
    }

    @Override
    public void created(ServerSession session,
                        Path path,
                        Map<String, ?> attrs,
                        Throwable thrown) throws IOException {
        createHandler.created(session, path, attrs, thrown);
    }
}
