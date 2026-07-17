package com.whatsoeversky.minder.sys.runner;

import com.whatsoeversky.minder.sys.service.SysUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
public class UserInitRunner implements ApplicationRunner {

    @Autowired
    private SysUserService sysUserService;

    @Override
    public void run(ApplicationArguments args) {
        sysUserService.initAdminUser();
    }
}
