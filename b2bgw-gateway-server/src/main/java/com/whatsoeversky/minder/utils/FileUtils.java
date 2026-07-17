package com.whatsoeversky.minder.utils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Set;

public class FileUtils {
    public static boolean isReadOption(Set<StandardOpenOption> options) {
        return options.contains(StandardOpenOption.READ);
    }

    public static boolean isWriteOption(Set<StandardOpenOption> options) {
        return options.contains(StandardOpenOption.WRITE) ||
                options.contains(StandardOpenOption.CREATE_NEW) ||
                options.contains(StandardOpenOption.CREATE) ||
                options.contains(StandardOpenOption.APPEND);
    }

    public static Long getFileSize(Path file) throws IOException {
        return Files.size(file);
    }
}
