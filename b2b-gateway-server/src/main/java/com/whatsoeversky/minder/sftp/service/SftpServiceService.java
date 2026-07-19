package com.whatsoeversky.minder.sftp.service;

import com.whatsoeversky.minder.sftp.dto.SftpServiceCreateReqDto;
import com.whatsoeversky.minder.sftp.dto.SftpServiceRespDto;
import com.whatsoeversky.minder.sftp.dto.SftpServiceUpdateReqDto;
import com.whatsoeversky.minder.sftp.entity.SftpService;
import com.whatsoeversky.minder.sftp.entity.SftpUser;
import com.whatsoeversky.minder.sftp.mapper.SftpServiceMapper;
import com.whatsoeversky.minder.sftp.repository.SftpServiceConfigRepository;
import com.whatsoeversky.minder.sftp.repository.SftpServiceRepository;
import com.whatsoeversky.minder.sftp.repository.SftpUserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class SftpServiceService {

    @Autowired
    private SftpServiceRepository sftpServiceRepository;

    @Autowired
    private SftpServiceConfigRepository sftpServiceConfigRepository;

    @Autowired
    private SftpUserRepository sftpUserRepository;

    @Autowired
    private SftpServiceMapper sftpServiceMapper;

    public List<SftpServiceRespDto> findAll() {
        List<SftpService> services = sftpServiceRepository.findAll();
        return services.stream()
                .map(this::toRespDtoWithUser)
                .collect(Collectors.toList());
    }

    public Optional<SftpService> findById(String id) {
        return sftpServiceRepository.findById(id);
    }

    public SftpServiceRespDto getService(String id) {
        SftpService service = sftpServiceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("服务不存在"));
        return toRespDtoWithUser(service);
    }

    public SftpServiceRespDto createService(SftpServiceCreateReqDto dto) {
        if (sftpServiceRepository.existsByName(dto.getName())) {
            throw new RuntimeException("服务名称已存在: " + dto.getName());
        }
        SftpService service = sftpServiceMapper.toEntity(dto);
        // 验证用户存在
        SftpUser user = sftpUserRepository.findById(service.getUserId())
                .orElseThrow(() -> new RuntimeException("用户不存在: " + service.getUserId()));
        service.setEnabled(true);
        service.setUserType(user.getUserType());
        service.setCreatedAt(LocalDateTime.now());
        service.setUpdatedAt(LocalDateTime.now());
        SftpService created = sftpServiceRepository.save(service);
        return sftpServiceMapper.toRespDto(created);
    }

    public SftpServiceRespDto updateService(String id, SftpServiceUpdateReqDto dto) {
        SftpService existing = sftpServiceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("服务不存在: " + id));
        sftpServiceMapper.updateEntity(dto, existing);
        // 验证用户存在
        SftpUser user = sftpUserRepository.findById(existing.getUserId())
                .orElseThrow(() -> new RuntimeException("用户不存在: " + existing.getUserId()));
        existing.setUserType(user.getUserType());
        existing.setUpdatedAt(LocalDateTime.now());

        SftpService updated = sftpServiceRepository.save(existing);
        return toRespDtoWithUser(updated);
    }

    public void deleteService(String id) {
        if (!sftpServiceRepository.existsById(id)) {
            throw new RuntimeException("服务不存在: " + id);
        }
        sftpServiceRepository.deleteById(id);
        sftpServiceConfigRepository.deleteAllByServiceId(id);
    }

    private SftpServiceRespDto toRespDtoWithUser(SftpService service) {
        SftpServiceRespDto resp = sftpServiceMapper.toRespDto(service);
        sftpUserRepository.findById(service.getUserId()).ifPresent(user -> {
            resp.setUsername(user.getUsername());
            resp.setUserType(user.getUserType());
        });
        return resp;
    }
}
