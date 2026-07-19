package com.whatsoeversky.minder.sftp.controller;

import com.whatsoeversky.minder.common.Result;
import com.whatsoeversky.minder.sftp.dto.SftpPasswordChangeReqDto;
import com.whatsoeversky.minder.sftp.dto.SftpUserCreateReqDto;
import com.whatsoeversky.minder.sftp.dto.SftpUserRespDto;
import com.whatsoeversky.minder.sftp.dto.SftpUserUpdateReqDto;
import com.whatsoeversky.minder.sftp.dto.SftpUserOptionDto;
import com.whatsoeversky.minder.sftp.service.SftpUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/sftp")
public class SftpUserController {

    @Autowired
    private SftpUserService sftpUserService;

    @GetMapping("/users")
    public Result<List<SftpUserRespDto>> listUsers() {
        return Result.success(sftpUserService.findAllResp());
    }

    @GetMapping("/users/{id}")
    public Result<SftpUserRespDto> getUser(@PathVariable String id) {
        return Result.success(sftpUserService.getUser(id));
    }

    @PostMapping("/users")
    public Result<SftpUserRespDto> createUser(@RequestBody SftpUserCreateReqDto dto) {
        return Result.success(sftpUserService.createUser(dto));
    }

    @PutMapping("/users/{id}")
    public Result<SftpUserRespDto> updateUser(@PathVariable String id, @RequestBody SftpUserUpdateReqDto dto) {
        return Result.success(sftpUserService.updateUser(id, dto));
    }

    @DeleteMapping("/users/{id}")
    public Result<Void> deleteUser(@PathVariable String id) {
        sftpUserService.deleteUser(id);
        return Result.success();
    }

    @PutMapping("/users/{id}/password")
    public Result<Void> changePassword(@PathVariable String id, @RequestBody SftpPasswordChangeReqDto dto) {
        sftpUserService.changePassword(id, dto.getNewPassword());
        return Result.success();
    }

    @PutMapping("/users/{id}/status")
    public Result<Void> toggleStatus(@PathVariable String id) {
        sftpUserService.toggleStatus(id);
        return Result.success();
    }

    @GetMapping("/users/options")
    public Result<List<SftpUserOptionDto>> getUserOptions() {
        return Result.success(sftpUserService.getUserOptions());
    }
}
