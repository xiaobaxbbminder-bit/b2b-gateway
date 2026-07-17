package com.whatsoeversky.minder.sys.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.whatsoeversky.minder.common.Result;
import com.whatsoeversky.minder.common.ResultCode;
import com.whatsoeversky.minder.sys.entity.SysUser;
import com.whatsoeversky.minder.sys.service.SysUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class SysUserController {

    @Autowired
    private SysUserService sysUserService;

    @PostMapping("/auth/login")
    public Result<Map<String, Object>> login(@RequestBody Map<String, String> loginRequest) {
        String username = loginRequest.get("username");
        String password = loginRequest.get("password");

        SysUser user = sysUserService.findByUsername(username)
                .orElse(null);

        if (user == null || !user.isEnabled()) {
            return Result.error(ResultCode.UNAUTHORIZED, "用户名或密码错误");
        }

        if (!sysUserService.verifyPassword(password, user.getPassword())) {
            return Result.error(ResultCode.UNAUTHORIZED, "用户名或密码错误");
        }

        StpUtil.login(user.getId());
        String token = StpUtil.getTokenValue();

        Map<String, Object> data = new HashMap<>();
        data.put("token", token);
        data.put("username", user.getUsername());
        return Result.success(data);
    }

    @PostMapping("/auth/logout")
    public Result<Void> logout() {
        StpUtil.logout();
        return Result.success();
    }
}
