package com.whatsoeversky.minder.sys.service;

import com.whatsoeversky.minder.sys.entity.SysUser;
import com.whatsoeversky.minder.sys.repository.SysUserRepository;
import org.mindrot.jbcrypt.BCrypt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class SysUserService {

    @Autowired
    private SysUserRepository sysUserRepository;

    public Optional<SysUser> findByUsername(String username) {
        return sysUserRepository.findByUsername(username);
    }

    public boolean verifyPassword(String rawPassword, String hashedPassword) {
        return BCrypt.checkpw(rawPassword, hashedPassword);
    }

    public SysUser createUser(SysUser user) {
        if (sysUserRepository.existsByUsername(user.getUsername())) {
            throw new RuntimeException("Username already exists: " + user.getUsername());
        }
        user.setPassword(BCrypt.hashpw(user.getPassword(), BCrypt.gensalt()));
        user.setEnabled(true);
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        return sysUserRepository.save(user);
    }

    public void initAdminUser() {
        if (!sysUserRepository.existsByUsername("admin")) {
            SysUser admin = SysUser.builder()
                    .username("admin")
                    .password(BCrypt.hashpw("123456", BCrypt.gensalt()))
                    .enabled(true)
                    .createdBy("system")
                    .updatedBy("system")
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build();
            sysUserRepository.save(admin);
        }
    }
}
