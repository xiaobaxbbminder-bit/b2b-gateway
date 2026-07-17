package com.whatsoeversky.minder.sys.runner;

import com.whatsoeversky.minder.sys.entity.SysDictItem;
import com.whatsoeversky.minder.sys.repository.SysDictItemRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class DictInitRunner implements ApplicationRunner {

    @Autowired
    private SysDictItemRepository sysDictItemRepository;

    @Override
    public void run(ApplicationArguments args) {
        if (sysDictItemRepository.existsByType("key_type")) return;

        sysDictItemRepository.saveAll(List.of(
                create("key_type", "RSA", "RSA", 1),
                create("key_type", "ECDSA", "ECDSA", 2),
                create("key_type", "ED25519", "ED25519", 3),

                create("host_key_algorithm", "ssh-rsa", "ssh-rsa", 1),
                create("host_key_algorithm", "rsa-sha2-256", "rsa-sha2-256", 2),
                create("host_key_algorithm", "rsa-sha2-512", "rsa-sha2-512", 3),

                create("public_key_algorithm", "ssh-rsa", "ssh-rsa", 1),
                create("public_key_algorithm", "rsa-sha2-256", "rsa-sha2-256", 2),
                create("public_key_algorithm", "rsa-sha2-512", "rsa-sha2-512", 3),

                create("kex_algorithm", "curve25519-sha256", "curve25519-sha256", 1),
                create("kex_algorithm", "ecdh-sha2-nistp256", "ecdh-sha2-nistp256", 2),
                create("kex_algorithm", "ecdh-sha2-nistp384", "ecdh-sha2-nistp384", 3),
                create("kex_algorithm", "ecdh-sha2-nistp521", "ecdh-sha2-nistp521", 4),
                create("kex_algorithm", "diffie-hellman-group-exchange-sha256", "diffie-hellman-group-exchange-sha256", 5),
                create("kex_algorithm", "diffie-hellman-group16-sha512", "diffie-hellman-group16-sha512", 6),
                create("kex_algorithm", "diffie-hellman-group18-sha512", "diffie-hellman-group18-sha512", 7),
                create("kex_algorithm", "diffie-hellman-group14-sha256", "diffie-hellman-group14-sha256", 8),

                create("encrypt_algorithm", "aes256-ctr", "aes256-ctr", 1),
                create("encrypt_algorithm", "aes192-ctr", "aes192-ctr", 2),
                create("encrypt_algorithm", "aes128-ctr", "aes128-ctr", 3),
                create("encrypt_algorithm", "aes256-gcm@openssh.com", "aes256-gcm@openssh.com", 4),
                create("encrypt_algorithm", "aes128-gcm@openssh.com", "aes128-gcm@openssh.com", 5),
                create("encrypt_algorithm", "chacha20-poly1305@openssh.com", "chacha20-poly1305@openssh.com", 6),

                create("filesystem_type", "local", "本地文件系统", 1),
                create("filesystem_type", "sftp", "SFTP 文件系统", 2),
                create("filesystem_type", "s3", "S3 文件系统", 3)
        ));
    }

    private SysDictItem create(String type, String key, String value, int sort) {
        return SysDictItem.builder().type(type).key(key).value(value).sort(sort).enabled(true).build();
    }
}
