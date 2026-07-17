package com.whatsoeversky.minder.agent.tools;

import com.whatsoeversky.minder.sftp.entity.SftpUser;
import com.whatsoeversky.minder.sftp.repository.SftpUserRepository;
import jakarta.annotation.Resource;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class SftpUserTools {
    @Resource
    private SftpUserRepository sftpUserRepository;

    @Tool(description = "根据某个用户名查找配置的账号密码")
    public String getPasswordByUsername(String username) {
        Optional<SftpUser> byUsername = sftpUserRepository.findByUsername(username);
        return byUsername.map(SftpUser::getPassword).orElse("");
    }
}
