package com.whatsoeversky.minder.utils;

import lombok.extern.slf4j.Slf4j;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.spec.MGF1ParameterSpec;
import java.security.spec.PSSParameterSpec;
import java.util.Base64;

/**
 * 数字签名的工具类，
 * PSS填充签名不可以使用非PSS填充验签，非PSS填充签名使用PSS填充验签也验证不通过，
 * 所以用户这边如果选了一种，后续需要更新（例如那天PSS填充也不安全了），那就寄了
 * PSS填充也有许多参数：主HASH，MGF_HASH，SALT长度
 */
@Slf4j
public class SignatureUtils {
    public static final String SHA1_WITH_RSA = "SHA1WithRSA";

    public static final String SHA256_WITH_RSA = "SHA256WithRSA";

    /**
     * 需要BC支持
     */
    public static final String SHA256_WITH_RSA_PSS = "SHA256WithRSAandMGF1";

    public static final String SHA512_WITH_RSA = "SHA512WithRSA";

    public static byte[] signV3(byte[] data, PrivateKey rsaPrivateKey) {
        try {
            Signature signature = Signature.getInstance(SHA256_WITH_RSA_PSS);
            signature.initSign(rsaPrivateKey);
            signature.update(data);
            return signature.sign();
        } catch (NoSuchAlgorithmException | InvalidKeyException | SignatureException e) {
            throw new RuntimeException(e);
        }
    }

    public static boolean verifyV3(byte[] data, byte[] signData, PublicKey rsaPublicKey) {
        try {
            Signature signature = Signature.getInstance(SHA256_WITH_RSA_PSS);
            signature.initVerify(rsaPublicKey);
            signature.update(data);
            return signature.verify(signData);
        } catch (NoSuchAlgorithmException | InvalidKeyException | SignatureException e) {
            log.error("error verify", e);
            return false;
        }
    }


    /**
     * 安全的用法：PSS填充
     *
     * @param data          data
     * @param rsaPrivateKey rsaPrivateKey
     * @return signed data
     */
    public static byte[] signV2(byte[] data, PrivateKey rsaPrivateKey) {
        try {
            Signature signature = Signature.getInstance("RSASSA-PSS");
            signature.setParameter(new PSSParameterSpec(
                    "SHA-256",
                    "MGF1",
                    MGF1ParameterSpec.SHA256,
                    32,
                    1
            ));
            signature.initSign(rsaPrivateKey);
            signature.update(data);
            return signature.sign();
        } catch (NoSuchAlgorithmException | InvalidKeyException | SignatureException |
                 InvalidAlgorithmParameterException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 安全的用法：PSS填充
     *
     * @param data         data
     * @param signData     signData
     * @param rsaPublicKey rsaPublicKey
     * @return 验证结果
     */
    public static boolean verifyV2(byte[] data, byte[] signData, PublicKey rsaPublicKey) {
        try {
            Signature signature = Signature.getInstance("RSASSA-PSS");
            signature.setParameter(new PSSParameterSpec(
                    "SHA-256",
                    "MGF1",
                    MGF1ParameterSpec.SHA256,
                    32,
                    1
            ));
            signature.initVerify(rsaPublicKey);
            signature.update(data);
            return signature.verify(signData);
        } catch (NoSuchAlgorithmException | InvalidKeyException | SignatureException |
                 InvalidAlgorithmParameterException e) {
            throw new RuntimeException(e);
        }
    }


    public static byte[] sign(byte[] data, PrivateKey rsaPrivateKey) {
        try {
            Signature signature = Signature.getInstance(SHA256_WITH_RSA);
            signature.initSign(rsaPrivateKey);
            signature.update(data);
            return signature.sign();
        } catch (NoSuchAlgorithmException | InvalidKeyException | SignatureException e) {
            throw new RuntimeException(e);
        }
    }

    public static boolean verify(byte[] data, byte[] signData, PublicKey rsaPublicKey) {
        try {
            Signature signature = Signature.getInstance(SHA256_WITH_RSA);
            signature.initVerify(rsaPublicKey);
            signature.update(data);
            return signature.verify(signData);
        } catch (NoSuchAlgorithmException | InvalidKeyException | SignatureException e) {
            log.error("error verify", e);
            return false;
        }
    }

    public static String signToString(byte[] data, PrivateKey rsaPrivateKey) {
        byte[] sign = sign(data, rsaPrivateKey);
        return Base64.getEncoder().encodeToString(sign);
    }

    public static void main(String[] args) {
        KeyPair keyPair = RSAUtils.generateKeyPair(2048);
        byte[] data = "我是你爸爸".getBytes(StandardCharsets.UTF_8);
        PrivateKey privateKey = keyPair.getPrivate();
        PublicKey publicKey = keyPair.getPublic();
        byte[] signData = sign(data, privateKey);
        System.out.println(Base64.getEncoder().encodeToString(signData));
        boolean verify = verify(data, signData, publicKey);
        System.out.println("verify res: " + verify);
        System.out.println("**********************************");
        signData = signV2(data, privateKey);
        System.out.println(Base64.getEncoder().encodeToString(signData));
        verify = verifyV2(data, signData, publicKey);
        System.out.println("verify res: " + verify);
        verify = verify(data, signData, publicKey);
        System.out.println("verify res: " + verify);
        System.out.println("**********************************");
        Security.addProvider(new BouncyCastleProvider());
        // SHA256WithRSAandMGF1 这种写法需要BC支持
        signData = signV3(data, privateKey);
        System.out.println(Base64.getEncoder().encodeToString(signData));
        verify = verifyV3(data, signData, publicKey);
        System.out.println("verify res: " + verify);
    }
}
