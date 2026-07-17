package com.whatsoeversky.minder.utils;


import org.apache.sshd.common.NamedResource;
import org.apache.sshd.common.config.keys.AuthorizedKeyEntry;
import org.apache.sshd.common.config.keys.FilePasswordProvider;
import org.apache.sshd.common.config.keys.PublicKeyEntry;
import org.apache.sshd.common.config.keys.loader.openssh.OpenSSHKeyPairResourceParser;
import org.apache.sshd.common.config.keys.writer.openssh.OpenSSHKeyEncryptionContext;
import org.apache.sshd.common.config.keys.writer.openssh.OpenSSHKeyPairResourceWriter;
import org.apache.sshd.common.session.SessionContext;
import org.apache.sshd.common.util.security.SecurityUtils;
import org.springframework.util.StringUtils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PublicKey;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

/**
 * 生成OpenSSH格式的公钥和私钥
 */
public class SSHDKeyUtils {
    /**
     * -----BEGIN OPENSSH PRIVATE KEY-----
     * b3BlbnNzaC1rZXktdjEAAAAACmFlczI1Ni1jdHIAAAAGYmNyeXB0AAAAGAAAABCA5yrRQI
     * 8DQlqrOv/R1SFJAAAAEAAAAAEAAAEXAAAAB3NzaC1yc2EAAAADAQABAAABAQDKhssHN1Tv
     * xhM2AfeT5QzNV2ucowKJsn2R/KJdZ6yPeXhcIy+jwXYry8Ji6KAVMWIlrvngBn0rvsl/A0
     * Tbl+IWbEBolPiO6Y6oHmxdznC7fJx6DVAXv9K2rAl2vsyypqnRmL+XUB0YqHkmeEySJY4O
     * oabK87kCYe4zbaSVFXiSEsY5udWjaJLiZd/85qkRWPv5cQSiJ2zhx3ec6O3hngHah3fXBG
     * IKfIl9leypGSxuapTdEkS4AC4cgiibTRQ/8C+R9da+Iyio5FrB2mtk4gRATRfJ5jF62gxG
     * DwBF2IOgyB6D6jWM/T3TbAftZdRoxp1wIw7l3mzv8o9Wvnw0Qq/xAAAD0EjMMb8VDez18a
     * ZGONvFucHwuSKaJXXhQ3heCLV0zFpcx2o64JMQJTZab0Cz1o3qHto81UkdYdsRCanKBgEM
     * ioIS4RhWibrpe4EzE/3ce3Uwav2oAyonI19UwqOj50xpFf5fa2jnyzLAPBWGFmw92zts5Y
     * tlibs4UM40voFWQNor0qDYKXRNvfj0LgnKybATD01WhjmRPnGBvv7QiC4DHcfJBCIv9Cpm
     * fuxfaxHphmv7jRW4VRdrb562Z6bgR3wCJjBs2UECBhw44RgsmQXWXv8JzLAOgneXd80/+T
     * KU3afy3sMkv00suV6mvXZiXn8yO0QKLSDNpBxAAa8ARBDM2+d1VHiSJS+thXQijKwKAhMY
     * vXcyybXCuYDeufVzUmiPGAzrmd515uhKvk3rNXweqHC17rcRzadyt9oZZvmiHMYgzXcOk/
     * sTgGUYDrVB97gT4OwX+Zg227U2W30lGOqExu906KTYnjSl95ZHu6EZzHvMCxJM5Y6OViyA
     * FWJw8sIJr6mTzyUlXKYcxplApVDV35FYeeRfTBwzQWMvTX7bigmJKlqyHUY9n6m/mF2YKW
     * 6LORmCdquUvqQgBSvHrFE8HJ5qCpdqLwdS2I/M1Xsmn1IxGSaMN499s0jHbPnYz9TR6Jw7
     * f+fC95BJRMIu9E1UspNujq+Ie9RQ1doKJp5ItpLdaC8E6dlj9X5pkeS+SJoEdY4ZhHXEvP
     * Y8FsX9TmsiZ76mp6P4XYCtm3+KAweDGAq1CCRVseNArvSIK76XfxAj1oIr8FWqlzG88tKu
     * 4C+IZzo4ubj9T7Vrbt+LeavEi5DwlfkiEcSC8Fi2dAT5rBi7ZGdgQTXqqy5ZwBUrOv3rUd
     * 2n2gIwEcNCgpZjSoLt6w4dGxIuAZJs7eEMiIaDf8uydoCJo5VqFlIB0HVYuV+REH3FV8tO
     * y0ObTuqCkkzVuMK2/IwsCD0b7GfpDt+lVeJa7RiFrw3B08HZpFFT1EkQj4c84qPJMBggo0
     * XuINLms3BjwNdmjfKc2ldtFcqLkKUdbxKLe03tzhe7O8PGmJkCplARqeJMYidkNhh+iurR
     * GjaByvVMkX1xLggYUqUP2khvHOhi4RXM9Qe8xyZ6AdV+xhaTcUB/XSY7pJMJAlQSHRwaND
     * MQBiNTDfLMTSRaHnvOrjxsPoHYmQxQNKVG64XEWEc03oEEcOmG4GPEd+oNbTphauYfHimt
     * Pr1/xNBMaQG066Ss+ZQYY42lnSHAn60EoGwfFA/BLF7nadb3CtI4sjy+I5SYFcyGhmcoBS
     * XV4xHGqb55PT5W3q1oENQIyjao4ns=
     * -----END OPENSSH PRIVATE KEY-----
     *
     * @param keyLength  keyLength
     * @param passphrase passphrase
     * @return MyKeyPair
     */
    public static MyKeyPair generateRSAKeyPair(int keyLength, String passphrase) {
        try {
            KeyPairGenerator keyPairGenerator = SecurityUtils.getKeyPairGenerator("rsa");
            keyPairGenerator.initialize(keyLength);
            KeyPair keyPair = keyPairGenerator.generateKeyPair();
            OpenSSHKeyPairResourceWriter openSSHKeyPairResourceWriter = new OpenSSHKeyPairResourceWriter();
            ByteArrayOutputStream privateKeyOut = new ByteArrayOutputStream();
            OpenSSHKeyEncryptionContext openSSHKeyEncryptionContext = null;
            if (StringUtils.hasLength(passphrase)) {
                openSSHKeyEncryptionContext = new OpenSSHKeyEncryptionContext();
                // 设置加密算法，推荐 aes256-ctr 或 aes256-gcm
                openSSHKeyEncryptionContext.setCipherName("aes");
                // 设置你的私钥解密密码
                openSSHKeyEncryptionContext.setPassword(passphrase);
                // 128,192,256
                openSSHKeyEncryptionContext.setCipherType("256");
                // CTR,GCM,CBC
                openSSHKeyEncryptionContext.setCipherMode("CTR");
            }
            openSSHKeyPairResourceWriter.writePrivateKey(keyPair, HostUtils.getLocalHostname(), openSSHKeyEncryptionContext, privateKeyOut);
            ByteArrayOutputStream publicKeyOut = new ByteArrayOutputStream();
            openSSHKeyPairResourceWriter.writePublicKey(keyPair, HostUtils.getLocalHostname(), publicKeyOut);
            return MyKeyPair.builder()
                    .privateKey(privateKeyOut.toString(StandardCharsets.UTF_8))
                    .publicKey(publicKeyOut.toString(StandardCharsets.UTF_8))
                    .build();
        } catch (GeneralSecurityException | IOException e) {
            throw new RuntimeException(e);
        }
    }


    public static MyKeyPair generateEcdsaKeyPair(int keyLength, String passphrase) {
        try {
            KeyPairGenerator keyPairGenerator = SecurityUtils.getKeyPairGenerator("ecdsa");
            keyPairGenerator.initialize(keyLength);
            KeyPair keyPair = keyPairGenerator.generateKeyPair();
            OpenSSHKeyPairResourceWriter openSSHKeyPairResourceWriter = new OpenSSHKeyPairResourceWriter();
            ByteArrayOutputStream privateKeyOut = new ByteArrayOutputStream();
            OpenSSHKeyEncryptionContext openSSHKeyEncryptionContext = null;
            if (StringUtils.hasLength(passphrase)) {
                openSSHKeyEncryptionContext = new OpenSSHKeyEncryptionContext();
                // 设置加密算法，推荐 aes256-ctr 或 aes256-gcm
                openSSHKeyEncryptionContext.setCipherName("aes");
                // 设置你的私钥解密密码
                openSSHKeyEncryptionContext.setPassword(passphrase);
                // 128,192,256
                openSSHKeyEncryptionContext.setCipherType("256");
                // CTR,GCM,CBC
                openSSHKeyEncryptionContext.setCipherMode("CTR");
            }
            openSSHKeyPairResourceWriter.writePrivateKey(keyPair, HostUtils.getLocalHostname(), openSSHKeyEncryptionContext, privateKeyOut);
            ByteArrayOutputStream publicKeyOut = new ByteArrayOutputStream();
            openSSHKeyPairResourceWriter.writePublicKey(keyPair, HostUtils.getLocalHostname(), publicKeyOut);
            return MyKeyPair.builder()
                    .privateKey(privateKeyOut.toString(StandardCharsets.UTF_8))
                    .publicKey(publicKeyOut.toString(StandardCharsets.UTF_8))
                    .build();
        } catch (GeneralSecurityException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static MyKeyPair generateEd25519KeyPair(int keyLength, String passphrase) {
        try {
            KeyPairGenerator keyPairGenerator = SecurityUtils.getKeyPairGenerator("ed25519");
            keyPairGenerator.initialize(keyLength);
            KeyPair keyPair = keyPairGenerator.generateKeyPair();
            OpenSSHKeyPairResourceWriter openSSHKeyPairResourceWriter = new OpenSSHKeyPairResourceWriter();
            ByteArrayOutputStream privateKeyOut = new ByteArrayOutputStream();
            OpenSSHKeyEncryptionContext openSSHKeyEncryptionContext = null;
            if (StringUtils.hasLength(passphrase)) {
                openSSHKeyEncryptionContext = new OpenSSHKeyEncryptionContext();
                // 设置加密算法，推荐 aes256-ctr 或 aes256-gcm
                // aes
                openSSHKeyEncryptionContext.setCipherName("aes");
                // 设置你的私钥解密密码
                openSSHKeyEncryptionContext.setPassword(passphrase);
                // 128,192,256
                openSSHKeyEncryptionContext.setCipherType("256");
                // CTR,GCM,CBC
                openSSHKeyEncryptionContext.setCipherMode("CTR");
            }
            openSSHKeyPairResourceWriter.writePrivateKey(keyPair, HostUtils.getLocalHostname(), openSSHKeyEncryptionContext, privateKeyOut);
            ByteArrayOutputStream publicKeyOut = new ByteArrayOutputStream();
            openSSHKeyPairResourceWriter.writePublicKey(keyPair, HostUtils.getLocalHostname(), publicKeyOut);
            return MyKeyPair.builder()
                    .privateKey(privateKeyOut.toString(StandardCharsets.UTF_8))
                    .publicKey(publicKeyOut.toString(StandardCharsets.UTF_8))
                    .build();
        } catch (GeneralSecurityException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 读取OpenSSH的私钥
     * -----BEGIN OPENSSH PRIVATE KEY-----
     * b3BlbnNzaC1rZXktdjEAAAAACmFlczI1Ni1jdHIAAAAGYmNyeXB0AAAAGAAAABDTtANyrK
     * 8djlDNcMC0XgJcAAAAEAAAAAEAAABoAAAAE2VjZHNhLXNoYTItbmlzdHAyNTYAAAAIbmlz
     * dHAyNTYAAABBBIzkrgvnNwQty25X7BOQovGH/1PcCYXlKViga5X2zT8ulSMNtFLYoy34MB
     * gDN201m9T5eNuohdPre+/5QPMpuyAAAACw/Z96yA9FA0862xn4cnVvjaUGpHq3vt0vfKve
     * OGvTBm7ABUDVG2tlWyM0sFMgM2J44Fq+TXa4FIRqpkzEb4MhgfDNUH7paPjE7GMtQQhXj3
     * xGK/adHZrmVCN1FDjks6QdfQ5SQTPnW61eqEj/QO1gYiAE01gPQtXfSpdpW6hKW1s1+CHJ
     * 3xdcny7JRvktslS8UfIt1CzQ8QPXqcH4lje+Pagerpx+OriJZCzCOlrNVPg=
     * -----END OPENSSH PRIVATE KEY-----
     *
     * @param openSshKey 私钥信息
     * @return KeyPair
     */
    public static KeyPair loadOpenSshKeyPair(String openSshKey, String passphrase) {
        try {
            List<String> lines = Arrays.asList(openSshKey.split("\\r?\\n"));
            Collection<KeyPair> keyPairs = OpenSSHKeyPairResourceParser.INSTANCE.loadKeyPairs(null, null, (session, resourceKey, retryIndex) -> passphrase, lines);
            if (keyPairs == null || keyPairs.isEmpty()) {
                throw new RuntimeException("No key pair found in OpenSSH key data");
            }
            return keyPairs.iterator().next();
        } catch (IOException | GeneralSecurityException e) {
            throw new RuntimeException("Failed to load OpenSSH key pair", e);
        }
    }

    public static PublicKey loadPublicKey(String publicKey) {
        try {
            PublicKeyEntry publicKeyEntry = AuthorizedKeyEntry.parsePublicKeyEntry(publicKey);
            return publicKeyEntry.resolvePublicKey(null, null, null);
        } catch (IOException | GeneralSecurityException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 支持PEM(PKCS#1)和OpenSSH头部格式
     *
     * @return KeyPair
     */
    public static KeyPair loadFromPrivateKey(String privateKey, String passphrase) {
        try {
            Iterable<KeyPair> keyPairs = SecurityUtils.loadKeyPairIdentities(null, null, new ByteArrayInputStream(privateKey.getBytes(StandardCharsets.UTF_8)), new FilePasswordProvider() {
                @Override
                public String getPassword(SessionContext session, NamedResource resourceKey, int retryIndex) throws IOException {
                    return passphrase;
                }
            });
            Iterator<KeyPair> iterator = keyPairs.iterator();
            if (iterator.hasNext()) {
                return iterator.next();
            }
            throw new RuntimeException("cannot get key pair");
        } catch (IOException | GeneralSecurityException e) {
            throw new RuntimeException(e);
        }
    }

    public static void main(String[] args) throws GeneralSecurityException, IOException {
        MyKeyPair myKeyPair1 = generateRSAKeyPair(2048, "123456");
        System.out.println(myKeyPair1.getPrivateKey());
        System.out.println(myKeyPair1.getPublicKey());

//        MyKeyPair myKeyPair2 = generateEd25519KeyPair(256, "123456");
//        System.out.println(myKeyPair2.getPrivateKey());
//        System.out.println(myKeyPair2.getPublicKey());
//
//        MyKeyPair myKeyPair3 = generateEcdsaKeyPair(256, "123456");
//        System.out.println(myKeyPair3.getPrivateKey());
//        System.out.println(myKeyPair3.getPublicKey());
//
//        MyKeyPair myKeyPair4 = generateEcdsaKeyPair(256, "");
//        System.out.println(myKeyPair4.getPrivateKey());
//        System.out.println(myKeyPair4.getPublicKey());
//        System.out.println(loadOpenSshKeyPair(myKeyPair4.getPrivateKey()));

        System.out.println(loadPublicKey("ecdsa-sha2-nistp256 AAAAE2VjZHNhLXNoYTItbmlzdHAyNTYAAAAIbmlzdHAyNTYAAABBBPC2/J5BkFdKI0AmE77V4gn2qxpwjXjknWxM3a6EcYKjtZ0jBEOT0vBsg8RSNCANOvQ0NicH3bcOZhVjnnr5xVc= whatsoeversky-MS-7B23\n"));
    }
}
