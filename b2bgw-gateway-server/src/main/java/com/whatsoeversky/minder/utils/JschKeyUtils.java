package com.whatsoeversky.minder.utils;

import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.KeyPair;
import org.springframework.util.StringUtils;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;

public class JschKeyUtils {
    /**
     * -----BEGIN RSA PRIVATE KEY-----
     * MIIG5AIBAAKCAYEA0qeYgYXafxcVm+1KDkRd6UJVDr8U6SCw6ixNB0nosLwgyFct
     * seJBccgmjmIx1zdiYIcTGaR19IaFYwz3ghXlD6ZYgSOHLAkLnqztf/ykmulMapzP
     * jkmwAG7bZIauycFc2J+q6UHLe7H4VFjFAj5uO+myHAyUYxWe7vFVRee6T0aaMwNg
     * wyz/p+rrsBrfiJwWeOM+AaUNnaXUu521JP5zaeotVrFa9K7a1ihIBV8ic2sIpWmj
     * jOER2yphiLaRDTX9DXAM77xBa74+OoT2tfRMV3X3mRhWGkhu456zEqMdkFzJmN2o
     * N3ajE6eg8K2LLYvXpWGqETS8ykH6SA20BNCmaE+ugc8uUeBUBZfZ1BwKgnIvp7+Z
     * k4+Uk/+Tug/id3iXM3S0sHUHY4IjsFbN0KzeJdSVwvhfu885IKbbXWIMK+ViNqZD
     * V8yPTuTrTO3QvBTCWf1Sa9iR6GCcvKj8+ym32sgRvO0vJL34iViabKh3uFkoSbhe
     * SOvbbP4C8/PN04Y3AgMBAAECggGADASGr/ZNxvavWL7FUky2hNewGDrGJnSjKIDl
     * gMHH1h6tdMXQdA0B3yFkMwxJZqCgv/wEwW+Upndu2v3tjYzSjWgtNWosTH1USIr6
     * CQAF+KvQRxFyH952qC0ANDB2KUlwPWWChR3So4MS3+ZT5Z21K+gtrT75z4X3aWSi
     * 5kLi6fc6VdMJV/5/CU/e0/mnK1koSQuXHoCRkcmuT2kfC/YTlIUebWCJ3Vqvs7cG
     * 634yw3M67ODldTnj4GUpvPhutwUPiPyzJ7QRDA3BKK4gSoh+kM238Y1XGeWi0D3+
     * 2s5JXKKc4asD3H69V4eSfFYZtnYOUlScFwbIp5uJKM6yl1mjbsWpaPqJs3u2v6aA
     * in6N8fSwmH7n8+W2o+VS8uJbbq07NHMJFaA4cnVDkbZyIZfy/2Ms++dwgQjhM9Ww
     * MHpyc8D30WvSSvLQtJ1niEc3s/og/N4HQkzVEEy8gJYdk+NwyuWfYHdWmc8fBuoM
     * ZEDu6jLa+c7RqbWOq4lw194XvkjNAoHBANd3WAoEfpmVlnhdTzUFLuZw8tAC48Vh
     * zTrn0bpVJ28rgxKE9gaxpQ2gKhxdDYsa6rc9bY4wHcLYzPfc1eBYkxKQAv63ydvb
     * t3VgS9ee4Zk/Meu/bW31bFBAALJmGfe0DXpLlCh6AsiVCk00ZoaXyH8Rw6M7yejF
     * BSci9Noz/DRNKqp/kXj3gVQrA498R/ce+DlmN1HHhLlxQq3FL1qB29Y6QPcJvu/P
     * er9DmyxC4fs74H2Nn8xdN6g4+Z3LA9XMOwKBwQD6SIiXk+ZHyxwmw6evzi9wtBS7
     * ZJ9P3Q3T+l/KF8otSmoZTYKxrmWRHDLDtFQxuF3YkQ2N8p+1rZ4sxGT98Epbb8xJ
     * 15agKt7owU3l6O/FhEvhfPmYR2thkdi91Lz7eK5DRapitjBMzFh2LflqDXd5CJzN
     * gj5Dgr2Y41AAsy9wPsRGAULQxuem7vuPp2BQEc7Ob5b3I++VZ0e1cqT3mcPTFv0r
     * ioNnFR63ML2uhVf9+MMlwH2+5sfv/WVc2duS2jUCgcBeZzGxoep+DCZTMj7D8jNs
     * T8xzCvEUyBcNWghv5WNTt/LDqHHK2QaCA8yvhK2sZuyzwDxB8h74Adk8OAuVMfmu
     * Rl6pj4jnxUqKKNjR7hDV/fQ3YDXn2YHNX11uzIFQIBlPndGaMmsJyxMqZG0PR21X
     * D0dyO33peHkbfptdBy3XFIA/F4l7QKXvZLE4qrE1OuJy87AFKq+iOFeInz5yHzdc
     * k0Vnd2Oq2nLjXhGcwOkWFFP1bpm3BpieIehsFEjn4YUCgcEAyUNcu++OJbt6Jt39
     * UY2PbqURauPujI1pWkYjk2LwDN2jVjeTWoiwWjF/zQqNDfKBXO7AJJ6vuawk30EL
     * KX16fe74K4U3NhNjb25K0xAvB/lr/6a9G6yK+EoNLhIN6nbQQ36ZFooFayyEEnzD
     * 7Xlo0qaPWV9b7HQNPQ3GPtnY0v1WXikTbsoUbJQPLSTeeWTole78Sj7WoaEM9Cd9
     * 6zH2XCBHdCUNp+4+5fdHATlvWecaHpidRc9VhnKReUojnQLlAoHBAL4BRxUz7dMg
     * SOZItqt8OYjPieGdwIpHsy1EChy9OyIcwwWeKkdd0SvgTsSvZ7WionN4JrXgJGoS
     * Um0TlrpI/O3fGSe7itKf8CfhZKLeEjLz7F24GTzPYbrNRDAs0aAM7c3kh0sEVwx6
     * NF/CprXeioe5HhWxhbBm7ABitwTwfL8rG84nJZDy0s8YpwyhjpMPDzZHesBsALzr
     * /lbNr7G8dDIpRrAdcJp7SoYpNwikWJ9Euu/rg9yTL8KkgaqR7BjZOQ==
     * -----END RSA PRIVATE KEY-----
     * <p>
     * ssh-rsa AAAAB3NzaC1yc2EAAAADAQABAAABgQDSp5iBhdp/FxWb7UoORF3pQlUOvxTpILDqLE0HSeiwvCDIVy2x4kFxyCaOYjHXN2JghxMZpHX0hoVjDPeCFeUPpliBI4csCQuerO1//KSa6UxqnM+OSbAAbttkhq7JwVzYn6rpQct7sfhUWMUCPm476bIcDJRjFZ7u8VVF57pPRpozA2DDLP+n6uuwGt+InBZ44z4BpQ2dpdS7nbUk/nNp6i1WsVr0rtrWKEgFXyJzawilaaOM4RHbKmGItpENNf0NcAzvvEFrvj46hPa19ExXdfeZGFYaSG7jnrMSox2QXMmY3ag3dqMTp6DwrYsti9elYaoRNLzKQfpIDbQE0KZoT66Bzy5R4FQFl9nUHAqCci+nv5mTj5ST/5O6D+J3eJczdLSwdQdjgiOwVs3QrN4l1JXC+F+7zzkgpttdYgwr5WI2pkNXzI9O5OtM7dC8FMJZ/VJr2JHoYJy8qPz7KbfayBG87S8kvfiJWJpsqHe4WShJuF5I69ts/gLz883Thjc= whatsoeversky-MS-7B23
     *
     * @param keyLength 私钥长度
     * @return 公钥和私钥
     */
    public static MyKeyPair generateRsaKeyPair(int keyLength, String passphrase) {
        try {
            JSch jSch = new JSch();
            KeyPair keyPair = KeyPair.genKeyPair(jSch, KeyPair.RSA, keyLength);
            return getMyKeyPair(keyPair, passphrase);
        } catch (JSchException exception) {
            throw new RuntimeException(exception);
        }
    }

    private static MyKeyPair getMyKeyPair(KeyPair keyPair, String passphrase) {
        ByteArrayOutputStream privateKeyOut = new ByteArrayOutputStream();
        if (StringUtils.hasLength(passphrase)) {
            keyPair.writePrivateKey(privateKeyOut, passphrase.getBytes(StandardCharsets.UTF_8));
        } else {
            keyPair.writePrivateKey(privateKeyOut);
        }
        ByteArrayOutputStream publicKeyOut = new ByteArrayOutputStream();
        keyPair.writePublicKey(publicKeyOut, HostUtils.getLocalHostname());
        return MyKeyPair.builder()
                .privateKey(privateKeyOut.toString(StandardCharsets.UTF_8))
                .publicKey(publicKeyOut.toString(StandardCharsets.UTF_8))
                .build();
    }

    /**
     * -----BEGIN EC PRIVATE KEY-----
     * MIHcAgEBBEIBJmv0ffjH9++aRcmihXsnThNNjDHi+FC5UFOkfcskUjdWe6WPJVm5
     * VwTiImrzf7A4Xj0Sx5d0uq3LbHT7aYXsFAugBwYFK4EEACOhgYkDgYYABAFMi/uG
     * LxzeYoy52G3WimOySqCyywOcgWk5hWmwF7Qnsdrxis0xRY9JCx93wF45/WuuthZe
     * L+2ke3BrVtTzf6N3dQHY37ZK/J4KVtOiHpFLuDWavO4jk6s94PCdvHVm0U2dyOP9
     * 0Z3orieD0bwHGwHpO9Aq0/aE/Dj14vccrBg2nXsH8w==
     * -----END EC PRIVATE KEY-----
     * <p>
     * ecdsa-sha2-nistp521 AAAAE2VjZHNhLXNoYTItbmlzdHA1MjEAAAAIbmlzdHA1MjEAAACFBAFMi/uGLxzeYoy52G3WimOySqCyywOcgWk5hWmwF7Qnsdrxis0xRY9JCx93wF45/WuuthZeL+2ke3BrVtTzf6N3dQHY37ZK/J4KVtOiHpFLuDWavO4jk6s94PCdvHVm0U2dyOP90Z3orieD0bwHGwHpO9Aq0/aE/Dj14vccrBg2nXsH8w== whatsoeversky-MS-7B23
     *
     * @param keyLength 秘钥长度
     * @return Ecdsa密钥对
     */
    public static MyKeyPair generateEcdsaKeyPair(int keyLength, String passphrase) {
        try {
            // 256,384,521
            JSch jSch = new JSch();
            KeyPair keyPair = KeyPair.genKeyPair(jSch, KeyPair.ECDSA, keyLength);
            return getMyKeyPair(keyPair, passphrase);
        } catch (JSchException exception) {
            throw new RuntimeException(exception);
        }
    }

    public static MyKeyPair generateEd25519KeyPair(int keyLength, String passphrase) {
        try {
            JSch jSch = new JSch();
            KeyPair keyPair = KeyPair.genKeyPair(jSch, KeyPair.ED25519, keyLength);
            return getMyKeyPair(keyPair, passphrase);
        } catch (JSchException exception) {
            throw new RuntimeException(exception);
        }
    }

    public static void main(String[] args) {
//        MyKeyPair myKeyPair1 = generateRsaKeyPair(3072, "");
//        System.out.println(myKeyPair1.getPrivateKey());
//        System.out.println(myKeyPair1.getPublicKey());
//        MyKeyPair myKeyPair2 = generateEcdsaKeyPair(521, "");
//        System.out.println(myKeyPair2.getPrivateKey());
//        System.out.println(myKeyPair2.getPublicKey());
        MyKeyPair myKeyPair3 = generateEd25519KeyPair(521, "");
        System.out.println(myKeyPair3.getPrivateKey());
        System.out.println(myKeyPair3.getPublicKey());
    }
}
