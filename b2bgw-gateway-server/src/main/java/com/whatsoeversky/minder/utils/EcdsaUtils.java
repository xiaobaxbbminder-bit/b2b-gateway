package com.whatsoeversky.minder.utils;

import java.security.*;
import java.security.spec.ECGenParameterSpec;
import java.util.Base64;

public class EcdsaUtils {
    public static MyKeyPair generateKeyPair(int keyLength) {
        try {
            KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("EC");
            ECGenParameterSpec ecGenParameterSpec = new ECGenParameterSpec("secp256r1");
            keyPairGenerator.initialize(ecGenParameterSpec,new SecureRandom());
            KeyPair keyPair = keyPairGenerator.generateKeyPair();
            PrivateKey privateKey = keyPair.getPrivate();
            PublicKey publicKey = keyPair.getPublic();
            return MyKeyPair.builder()
                    .privateKey(Base64.getEncoder().encodeToString(privateKey.getEncoded()))
                    .publicKey(Base64.getEncoder().encodeToString(publicKey.getEncoded()))
                    .build();
        } catch (NoSuchAlgorithmException | InvalidAlgorithmParameterException e) {
            throw new RuntimeException(e);
        }
    }

    public static void main(String[] args) {
        System.out.println(generateKeyPair(256));
    }
}
