package com.whatsoeversky.minder.sftp.service;

import com.whatsoeversky.minder.sftp.dto.SftpServiceConfigReqDto;
import com.whatsoeversky.minder.sftp.dto.SftpServiceConfigRespDto;
import com.whatsoeversky.minder.sftp.entity.SftpService;
import com.whatsoeversky.minder.sftp.entity.SftpServiceConfig;
import com.whatsoeversky.minder.sftp.mapper.SftpServiceConfigMapper;
import com.whatsoeversky.minder.sftp.repository.SftpServiceConfigRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class SftpServiceConfigService {

    @Autowired
    private SftpServiceConfigRepository sftpServiceConfigRepository;

    @Autowired
    private SftpServiceService sftpServiceService;

    @Autowired
    private SftpServiceConfigMapper sftpServiceConfigMapper;

    public SftpServiceConfigRespDto getConfig(String serviceId) {
        sftpServiceService.findById(serviceId)
                .orElseThrow(() -> new RuntimeException("服务不存在: " + serviceId));
        SftpServiceConfig config = sftpServiceConfigRepository.findByServiceId(serviceId)
                .orElseGet(() -> SftpServiceConfig.builder().serviceId(serviceId).plugins(List.of()).build());
        return sftpServiceConfigMapper.toRespDto(config);
    }

    public SftpServiceConfigRespDto saveConfig(String serviceId, SftpServiceConfigReqDto dto) {
        SftpService service = sftpServiceService.findById(serviceId)
                .orElseThrow(() -> new RuntimeException("服务不存在: " + serviceId));
        SftpServiceConfig config = sftpServiceConfigMapper.toEntity(dto);
        config.setUserId(service.getUserId());
        config.setServiceId(serviceId);
        SftpServiceConfig existing = sftpServiceConfigRepository.findByServiceId(serviceId).orElse(null);
        if (existing != null) {
            config.setId(existing.getId());
            config.setCreatedAt(existing.getCreatedAt());
            config.setUpdatedAt(LocalDateTime.now());
        } else {
            config.setCreatedAt(LocalDateTime.now());
            config.setUpdatedAt(LocalDateTime.now());
        }
        SftpServiceConfig saved = sftpServiceConfigRepository.save(config);
        return sftpServiceConfigMapper.toRespDto(saved);
    }
}
