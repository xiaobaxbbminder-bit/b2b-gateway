package com.whatsoeversky.minder.sftp.service;

import com.whatsoeversky.minder.sftp.dto.SftpUserCreateReqDto;
import com.whatsoeversky.minder.sftp.dto.SftpUserOptionDto;
import com.whatsoeversky.minder.sftp.dto.SftpUserRespDto;
import com.whatsoeversky.minder.sftp.dto.SftpUserUpdateReqDto;
import com.whatsoeversky.minder.sftp.entity.SftpTempKeypair;
import com.whatsoeversky.minder.sftp.entity.SftpUser;
import com.whatsoeversky.minder.sftp.mapper.SftpUserMapper;
import com.whatsoeversky.minder.sftp.repository.SftpTempKeypairRepository;
import com.whatsoeversky.minder.sftp.repository.SftpUserRepository;
import com.whatsoeversky.minder.utils.SSHDKeyUtils;
import org.mindrot.jbcrypt.BCrypt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class SftpUserService {

    @Autowired
    private SftpUserRepository sftpUserRepository;

    @Autowired
    private SftpUserMapper sftpUserMapper;

    @Autowired
    private SftpTempKeypairRepository sftpTempKeypairRepository;

    public List<SftpUserRespDto> findAllResp() {
        return sftpUserRepository.findAll().stream()
                .map(sftpUserMapper::toRespDto)
                .collect(Collectors.toList());
    }

    public List<SftpUserOptionDto> getUserOptions() {
        return sftpUserRepository.findAll().stream()
                .map(user -> SftpUserOptionDto.builder()
                        .id(user.getId())
                        .username(user.getUsername())
                        .userType(user.getUserType())
                        .build())
                .collect(Collectors.toList());
    }

    public SftpUserRespDto getUser(String id) {
        SftpUser user = sftpUserRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
        return sftpUserMapper.toRespDto(user);
    }

    public SftpUserRespDto createUser(SftpUserCreateReqDto dto) {
        SftpUser user = sftpUserMapper.toEntity(dto);
        resolveKeypair(dto.getUserType(), dto.getKeypairId(), user);
        if (sftpUserRepository.existsByUsername(user.getUsername())) {
            throw new RuntimeException("用户名已存在: " + user.getUsername());
        }
        if (!"client".equals(user.getUserType())) {
            validatePublicKey(user.getPublicKey());
        }
        user.setPassword(BCrypt.hashpw(user.getPassword(), BCrypt.gensalt()));
        user.setEnabled(true);
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        SftpUser created = sftpUserRepository.save(user);
        return sftpUserMapper.toRespDto(created);
    }

    public SftpUserRespDto updateUser(String id, SftpUserUpdateReqDto dto) {
        SftpUser user = sftpUserRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
        String userType = user.getUserType();
        sftpUserMapper.updateEntity(dto, user);
        resolveKeypair(userType, dto.getKeypairId(), user);
        if (!"client".equals(userType)) {
            validatePublicKey(user.getPublicKey());
        }
        user.setEnabled(Boolean.TRUE);
        user.setUpdatedAt(LocalDateTime.now());
        SftpUser updated = sftpUserRepository.save(user);
        return sftpUserMapper.toRespDto(updated);
    }

    public void deleteUser(String id) {
        if (!sftpUserRepository.existsById(id)) {
            throw new RuntimeException("用户不存在");
        }
        sftpUserRepository.deleteById(id);
    }

    public void changePassword(String id, String newPassword) {
        SftpUser user = sftpUserRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
        user.setPassword(BCrypt.hashpw(newPassword, BCrypt.gensalt()));
        user.setUpdatedAt(LocalDateTime.now());
        sftpUserRepository.save(user);
    }

    public void toggleStatus(String id) {
        SftpUser user = sftpUserRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
        user.setEnabled(user.getEnabled() != null && !user.getEnabled());
        user.setUpdatedAt(LocalDateTime.now());
        sftpUserRepository.save(user);
    }


    public void initAdminUser() {
        if (!sftpUserRepository.existsByUsername("admin")) {
            SftpUser admin = SftpUser.builder()
                    .username("admin")
                    .password(BCrypt.hashpw("123456", BCrypt.gensalt()))
                    .enabled(true)
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build();
            sftpUserRepository.save(admin);
        }
    }

    private void resolveKeypair(String userType, String keypairId, SftpUser user) {
        if ("client".equals(userType) && keypairId != null && !keypairId.isBlank()) {
            SftpTempKeypair keypair = sftpTempKeypairRepository.findById(keypairId)
                    .orElseThrow(() -> new RuntimeException("密钥对不存在: " + keypairId));
            user.setPublicKey(keypair.getPublicKey());
            user.setPrivateKey(keypair.getPrivateKey());
            user.setKeyType(keypair.getKeyType());
        }
    }

    private void validatePublicKey(String publicKey) {
        if (publicKey != null && !publicKey.isBlank()) {
            try {
                SSHDKeyUtils.loadPublicKey(publicKey.trim());
            } catch (Exception e) {
                throw new RuntimeException("公钥格式无效: " + e.getMessage());
            }
        }
    }
}
