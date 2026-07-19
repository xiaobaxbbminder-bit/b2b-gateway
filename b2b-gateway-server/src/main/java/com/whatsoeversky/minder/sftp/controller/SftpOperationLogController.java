package com.whatsoeversky.minder.sftp.controller;

import com.whatsoeversky.minder.common.Result;
import com.whatsoeversky.minder.sftp.entity.SftpOperationDetailLog;
import com.whatsoeversky.minder.sftp.entity.SftpOperationLog;
import com.whatsoeversky.minder.sftp.repository.SftpOperationDetailLogRepository;
import com.whatsoeversky.minder.sftp.repository.SftpOperationLogRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/sftp")
public class SftpOperationLogController {

    @Autowired
    private SftpOperationLogRepository operationLogRepository;

    @Autowired
    private SftpOperationDetailLogRepository operationDetailLogRepository;

    @GetMapping("/operation-logs")
    public Result<Page<SftpOperationLog>> listLogs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return Result.success(operationLogRepository.findAllByOrderByStartTimeDesc(pageable));
    }

    @GetMapping("/operation-logs/{id}")
    public Result<SftpOperationLog> getLog(@PathVariable String id) {
        SftpOperationLog log = operationLogRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("日志不存在"));
        return Result.success(log);
    }

    @GetMapping("/operation-logs/{id}/details")
    public Result<List<SftpOperationDetailLog>> getLogDetails(@PathVariable String id) {
        return Result.success(operationDetailLogRepository.findByLogIdOrderByLogTimeAsc(id));
    }

    @DeleteMapping("/operation-logs/{id}")
    public Result<Void> deleteLog(@PathVariable String id) {
        operationLogRepository.deleteById(id);
        operationDetailLogRepository.findByLogIdOrderByLogTimeAsc(id)
                .forEach(detail -> operationDetailLogRepository.deleteById(detail.getId()));
        return Result.success();
    }
}
