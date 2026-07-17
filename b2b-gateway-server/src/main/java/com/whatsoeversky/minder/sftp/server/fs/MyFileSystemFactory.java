package com.whatsoeversky.minder.sftp.server.fs;

import org.apache.sshd.common.file.FileSystemFactory;
import org.apache.sshd.common.file.root.RootedFileSystemProvider;
import org.apache.sshd.common.session.SessionContext;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.*;
import java.util.Collections;

@Component
public class MyFileSystemFactory implements FileSystemFactory {

    @Override
    public Path getUserHomeDir(SessionContext session) throws IOException {
        String username = session.getUsername();
        var userHome = Paths.get("/home/whatsoeversky/sftp/", username);
        if (!Files.exists(userHome)) {
            Files.createDirectories(userHome);
        }
        return userHome;
    }

    @Override
    public FileSystem createFileSystem(SessionContext session) throws IOException {
        Path dir = getUserHomeDir(session);
        if (dir == null) {
            throw new InvalidPathException(session.getUsername(), "Cannot resolve home directory");
        }

        return new RootedFileSystemProvider().newFileSystem(dir, Collections.emptyMap());
    }
}
