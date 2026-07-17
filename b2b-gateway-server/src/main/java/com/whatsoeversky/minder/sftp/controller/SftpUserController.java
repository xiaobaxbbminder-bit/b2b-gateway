package com.whatsoeversky.minder.sftp.controller;

import com.whatsoeversky.minder.common.Result;
import com.whatsoeversky.minder.sftp.dto.PasswordChangeReqDto;
import com.whatsoeversky.minder.sftp.dto.SftpUserCreateReqDto;
import com.whatsoeversky.minder.sftp.dto.SftpUserRespDto;
import com.whatsoeversky.minder.sftp.dto.SftpUserUpdateReqDto;
import com.whatsoeversky.minder.sftp.dto.UserOptionDto;
import com.whatsoeversky.minder.sftp.service.SftpUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class SftpUserController {

    @Autowired
    private SftpUserService sftpUserService;

    @GetMapping("/sftp-users")
    public Result<List<SftpUserRespDto>> listUsers() {
        return Result.success(sftpUserService.findAllResp());
    }

    @GetMapping("/sftp-users/{id}")
    public Result<SftpUserRespDto> getUser(@PathVariable String id) {
        return Result.success(sftpUserService.getUser(id));
    }

    @PostMapping("/sftp-users")
    public Result<SftpUserRespDto> createUser(@RequestBody SftpUserCreateReqDto dto) {
        return Result.success(sftpUserService.createUser(dto));
    }

    @PutMapping("/sftp-users/{id}")
    public Result<SftpUserRespDto> updateUser(@PathVariable String id, @RequestBody SftpUserUpdateReqDto dto) {
        return Result.success(sftpUserService.updateUser(id, dto));
    }

    @DeleteMapping("/sftp-users/{id}")
    public Result<Void> deleteUser(@PathVariable String id) {
        sftpUserService.deleteUser(id);
        return Result.success();
    }

    @PutMapping("/sftp-users/{id}/password")
    public Result<Void> changePassword(@PathVariable String id, @RequestBody PasswordChangeReqDto dto) {
        sftpUserService.changePassword(id, dto.getNewPassword());
        return Result.success();
    }

    @PutMapping("/sftp-users/{id}/status")
    public Result<Void> toggleStatus(@PathVariable String id) {
        sftpUserService.toggleStatus(id);
        return Result.success();
    }

    @GetMapping("/sftp-users/options")
    public Result<List<UserOptionDto>> getUserOptions() {
        return Result.success(sftpUserService.getUserOptions());
    }
}
