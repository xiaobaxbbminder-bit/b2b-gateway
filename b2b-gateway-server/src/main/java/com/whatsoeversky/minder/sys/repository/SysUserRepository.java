package com.whatsoeversky.minder.sys.repository;

import com.whatsoeversky.minder.sys.entity.SysUser;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface SysUserRepository extends MongoRepository<SysUser, String> {
    Optional<SysUser> findByUsername(String username);
    boolean existsByUsername(String username);
}
