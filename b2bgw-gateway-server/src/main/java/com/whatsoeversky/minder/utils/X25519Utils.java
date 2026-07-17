package com.whatsoeversky.minder.utils;

import lombok.extern.slf4j.Slf4j;
import org.bouncycastle.crypto.params.X25519PrivateKeyParameters;
import org.bouncycastle.crypto.params.X25519PublicKeyParameters;

import java.util.Arrays;
import java.util.Base64;

@Slf4j
public class X25519Utils {
    public static boolean matchX25519(String privateKeyStr,
                                      String publicKeyStr) throws Exception {
        try {
            byte[] privateBytes = decodeBase64(privateKeyStr);
            byte[] publicBytes = decodeBase64(publicKeyStr);

            if (privateBytes.length != 32 || publicBytes.length != 32) {
                log.info("❌ 密钥长度错误！X25519 密钥原始长度必须严格为 32 字节。");
                return false;
            }

            X25519PrivateKeyParameters privParams = new X25519PrivateKeyParameters(privateBytes, 0);
            X25519PublicKeyParameters pubParams = privParams.generatePublicKey();
            byte[] derivedPublicBytes = pubParams.getEncoded();

            if (Arrays.equals(derivedPublicBytes, publicBytes)) {
                log.info("✅ 匹配成功！公钥和私钥是一对。");
                return true;
            } else {
                log.info("❌ 匹配失败！它们不是一对。");
                return false;
            }
        } catch (Exception e) {
            log.error("❌ 密钥解析失败，请确认是否为合法的 Base64 格式。", e);
            return false;
        }
    }

    private static byte[] decodeBase64(String key) {
        StringBuilder normalized = new StringBuilder(key.replace('-', '+').replace('_', '/').trim());
        // 补齐 Base64 缺少的等号 =
        while (normalized.length() % 4 != 0) {
            normalized.append("=");
        }
        return Base64.getDecoder().decode(normalized.toString());
    }

    public static void main(String[] args) throws Exception {
        boolean matched = matchX25519("aH6d8vIhNyuHHmjNNrWrwwGhvWkcoqxViZBf4ttVXFc", "jJLFdFUXZb8v5cKIpv5rMw8qhAUInY1ssliogEnELFI");
        System.out.println(matched);
    }
}
