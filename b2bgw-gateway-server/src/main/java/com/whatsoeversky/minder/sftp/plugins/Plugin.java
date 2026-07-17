package com.whatsoeversky.minder.sftp.plugins;

import com.whatsoeversky.minder.sftp.support.FileRunContext;

import java.io.IOException;

public interface Plugin {
    String getPluginName();

    void execute(FileRunContext context, String args) throws IOException;
}
