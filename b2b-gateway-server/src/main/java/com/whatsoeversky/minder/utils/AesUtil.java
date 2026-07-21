package com.whatsoeversky.minder.utils;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;

public class AesUtil {

    private static final String ALGORITHM = "AES/GCM/NoPadding";
    // 密钥长度 256 位 (也可选 128)
    private static final int KEY_SIZE = 256;
    // GCM 推荐 IV 长度为 12 字节 (96位)
    private static final int IV_LENGTH_BYTES = 12;
    // GCM 认证 Tag 长度为 128 位
    private static final int TAG_LENGTH_BIT = 128;

    // 1. 生成随机 AES 密钥
    public static SecretKey generateKey() throws Exception {
        KeyGenerator keyGen = KeyGenerator.getInstance("AES");
        keyGen.init(KEY_SIZE, new SecureRandom());
        return keyGen.generateKey();
    }

    public static String exportKeyToBase64(SecretKey key) {
        byte[] rawKey = key.getEncoded();
        return Base64.getEncoder().encodeToString(rawKey);
    }

    public static SecretKey importKeyFromBase64(String base64Key) {
        byte[] decodedKey = Base64.getDecoder().decode(base64Key);
        return new SecretKeySpec(decodedKey, 0, decodedKey.length, "AES");
    }

    // 2. 加密：将 IV 拼接到密文最前方，方便后续解密
    public static String encrypt(String plainText, SecretKey key) throws Exception {
        byte[] iv = new byte[IV_LENGTH_BYTES];
        // 每次加密必须使用全新的随机 IV
        new SecureRandom().nextBytes(iv);

        Cipher cipher = Cipher.getInstance(ALGORITHM);
        GCMParameterSpec parameterSpec = new GCMParameterSpec(TAG_LENGTH_BIT, iv);
        cipher.init(Cipher.ENCRYPT_MODE, key, parameterSpec);

        byte[] cipherText = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));

        // 将 IV + CipherText 打包到一个字节数组中（Base64 编码输出）
        ByteBuffer byteBuffer = ByteBuffer.allocate(iv.length + cipherText.length);
        byteBuffer.put(iv);
        byteBuffer.put(cipherText);

        return Base64.getEncoder().encodeToString(byteBuffer.array());
    }

    // 3. 解密：从密文中提取 IV，再进行解密
    public static String decrypt(String base64EncryptedData, SecretKey key) throws Exception {
        byte[] decodedData = Base64.getDecoder().decode(base64EncryptedData);

        // 提取 IV
        ByteBuffer byteBuffer = ByteBuffer.wrap(decodedData);
        byte[] iv = new byte[IV_LENGTH_BYTES];
        byteBuffer.get(iv);

        // 提取真正的密文
        byte[] cipherText = new byte[byteBuffer.remaining()];
        byteBuffer.get(cipherText);

        Cipher cipher = Cipher.getInstance(ALGORITHM);
        GCMParameterSpec parameterSpec = new GCMParameterSpec(TAG_LENGTH_BIT, iv);
        cipher.init(Cipher.DECRYPT_MODE, key, parameterSpec);

        byte[] plainTextBytes = cipher.doFinal(cipherText);
        return new String(plainTextBytes, StandardCharsets.UTF_8);
    }

    public static void main(String[] args) throws Exception {
        SecretKey secretKey = generateKey();
        System.out.println(exportKeyToBase64(secretKey));
    }

    public static String encrypt(String plainText, String base64Key) throws Exception {
        SecretKey key = importKeyFromBase64(base64Key);
        return encrypt(plainText, key);
    }

    public static String decrypt(String encryptedBase64, String base64Key) throws Exception {
        SecretKey key = importKeyFromBase64(base64Key);
        return decrypt(encryptedBase64, key);
    }
}