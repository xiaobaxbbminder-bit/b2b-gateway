package com.whatsoeversky.minder.sftp.plugins;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.whatsoeversky.minder.sftp.support.FileRunContext;
import com.whatsoeversky.minder.utils.DigestUtils;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Path;

@Slf4j
@Component
public class CheckSumPlugin implements Plugin {
    @Resource
    private ObjectMapper objectMapper;

    @Override
    public String getPluginName() {
        return "checksum";
    }

    @Override
    public void execute(FileRunContext context, String args) throws IOException {
        CheckSumArg checkSumArg = objectMapper.readValue(args, CheckSumArg.class);
        Path file = context.getFile();
        try {
            String fileDigest = DigestUtils.getFileDigest(file, checkSumArg.getAlgorithm());
            FileRunContext.ContextVariable contextVariables = FileRunContext.ContextVariable.builder()
                    .args(checkSumArg)
                    .res(fileDigest)
                    .build();
            context.putContextVariables(getPluginName(), contextVariables);
        } catch (Exception exception) {
            log.error("error get file digest", exception);
            throw new IOException(exception.getMessage());
        }
    }
}
