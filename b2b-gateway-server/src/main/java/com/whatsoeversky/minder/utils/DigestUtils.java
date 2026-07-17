package com.whatsoeversky.minder.utils;

import lombok.Data;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

@Data
public class DigestUtils {
    public static char[] HEX_CHAR = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};

    /**
     * 获取文件摘要
     *
     * @param file      file
     * @param algorithm md5,sha256
     * @return digest
     * @throws NoSuchAlgorithmException NoSuchAlgorithmException
     * @throws IOException              IOException
     */
    public static String getFileDigest(Path file, String algorithm) throws NoSuchAlgorithmException, IOException {
        MessageDigest messageDigest = MessageDigest.getInstance(algorithm);
        byte[] buffer = new byte[1024 * 8];
        int length;
        try (InputStream inputStream = Files.newInputStream(file)) {
            while ((length = inputStream.read(buffer)) != -1) {
                messageDigest.update(buffer, 0, length);
            }
        }
        byte[] digest = messageDigest.digest();
        return byteToHex(digest);
    }

    private static String byteToHex(byte[] digest) {
        StringBuilder res = new StringBuilder();
        for (byte b : digest) {
            int heightIndex = b >> 4 & 0xF;
            res.append(HEX_CHAR[heightIndex]);
            int lowIndex = b & 0xF;
            res.append(HEX_CHAR[lowIndex]);
        }
        return res.toString();
    }


    public static String sha256Hex(byte[] data) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(md.digest(data));
        } catch (Exception e) {
            throw new RuntimeException("SHA-256 not available", e);
        }
    }

    public static String sha256Hex(String input) {
        return DigestUtils.sha256Hex(input.getBytes(StandardCharsets.UTF_8));
    }

    public static String sha256Hex(InputStream inputStream) throws IOException, NoSuchAlgorithmException {
        MessageDigest messageDigest = MessageDigest.getInstance("SHA256");
        byte[] buffer = new byte[1024 * 8];
        int length;
        while ((length = inputStream.read(buffer)) != -1) {
            messageDigest.update(buffer, 0, length);
        }
        byte[] digest = messageDigest.digest();
        return byteToHex(digest);
    }


    public static void main(String[] args) throws NoSuchAlgorithmException, IOException {
//        System.out.println(getFileDigest(Path.of("/home/whatsoeversky/下载/steam_latest.deb"), "sha512"));
//        System.out.println(getFileDigest(Path.of("/home/whatsoeversky/下载/steam_latest.deb"), "sha256"));
//        System.out.println(getFileDigest(Path.of("/home/whatsoeversky/下载/steam_latest.deb"), "sha1"));
//        System.out.println(getFileDigest(Path.of("/home/whatsoeversky/下载/steam_latest.deb"), "md5"));
    }
}
