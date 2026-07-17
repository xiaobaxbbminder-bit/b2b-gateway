package com.whatsoeversky.minder.utils;

import lombok.extern.slf4j.Slf4j;

import java.math.BigInteger;
import java.security.*;
import java.security.interfaces.RSAPrivateCrtKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.RSAPublicKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

/**
 * RSA秘钥对生成
 */
@Slf4j
public class RSAUtils {

    public static KeyPair generateKeyPair(int keyLength) {
        try {
            KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
            keyPairGenerator.initialize(keyLength);
            return keyPairGenerator.generateKeyPair();
        } catch (NoSuchAlgorithmException exception) {
            throw new RuntimeException(exception);
        }
    }

    public static MyKeyPair generate(int keyLength) {
        KeyPair keyPair = generateKeyPair(keyLength);
        PrivateKey privateKey = keyPair.getPrivate();
        byte[] privateKeyEncoded = privateKey.getEncoded();
        log.info("private key length: {}", privateKeyEncoded.length);
        PublicKey publicKey = keyPair.getPublic();
        Base64.Encoder encoder = Base64.getEncoder();
        return MyKeyPair.builder()
                .privateKey(encoder.encodeToString(privateKeyEncoded))
                .publicKey(encoder.encodeToString(publicKey.getEncoded()))
                .build();
    }


    public static PrivateKey loadPrivateKey(String privateKey) {
        try {
            PKCS8EncodedKeySpec pkcs8EncodedKeySpec = new PKCS8EncodedKeySpec(Base64.getDecoder().decode(privateKey));
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            return keyFactory.generatePrivate(pkcs8EncodedKeySpec);
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new RuntimeException(e);
        }
    }

    public static PublicKey loadPublicKey(String publicKey) {
        try {
            byte[] publicKeyBytes = Base64.getDecoder().decode(publicKey);
            X509EncodedKeySpec x509EncodedKeySpec = new X509EncodedKeySpec(publicKeyBytes);
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            return keyFactory.generatePublic(x509EncodedKeySpec);
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new RuntimeException(e);
        }
    }

    public static PublicKey getPublicKeyFromPrivateKey(PrivateKey privateKey) {
        if (privateKey instanceof RSAPrivateCrtKey rsaPrivateCrtKey) {
            try {
                BigInteger modulus = rsaPrivateCrtKey.getModulus();
                BigInteger publicExponent = rsaPrivateCrtKey.getPublicExponent();
                RSAPublicKeySpec rsaPublicKeySpec = new RSAPublicKeySpec(modulus, publicExponent);
                KeyFactory keyFactory = KeyFactory.getInstance("RSA");
                return keyFactory.generatePublic(rsaPublicKeySpec);
            } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
                throw new RuntimeException(e);
            }
        }
        throw new RuntimeException("cannot get public key from private key");
    }

    public static PublicKey getPublicKeyFromPrivateKey(String privateKey) {
        PrivateKey pk = loadPrivateKey(privateKey);
        return getPublicKeyFromPrivateKey(pk);
    }


    public static void main(String[] args) {
        MyKeyPair keyPair = generate(2048);
        System.out.println(keyPair.getPrivateKey());
        System.out.println(keyPair.getPublicKey());
        PrivateKey privateKey = loadPrivateKey(keyPair.getPrivateKey());
        PublicKey publicKey = loadPublicKey(keyPair.getPublicKey());
        System.out.println(privateKey);
        System.out.println(publicKey);
        KeyPair kp = new KeyPair(null, privateKey);
        System.out.println(kp.getPrivate());
        System.out.println(kp.getPublic());
        System.out.println(kp);
        System.out.println(getPublicKeyFromPrivateKey(keyPair.getPrivateKey()));
    }
}
