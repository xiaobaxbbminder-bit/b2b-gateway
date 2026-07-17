package com.whatsoeversky.minder.utils;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.URI;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.HexFormat;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
public class S3SignatureUtils {
    public static final String EMPTY_PAYLOAD_SHA256 = "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855";
    public static final String SERVICE = "s3";
    public static final String ALGORITHM = "AWS4-HMAC-SHA256";
    public static final String AWS4_REQUEST = "aws4_request";


    public static String sign(String requestMethod,
                              String canonicalUri,
                              Map<String, String> queryParams,
                              Map<String, String> headers,
                              String region,
                              String dateStamp,
                              String dateTime,
                              String contentSha256,
                              String accessKey,
                              String secretKey) {
        String canonicalQueryString = S3SignatureUtils.buildCanonicalQueryString(queryParams);
        String canonicalHeaders = S3SignatureUtils.buildCanonicalHeaders(headers);
        String signedHeaders = S3SignatureUtils.buildSignedHeaders(headers);
        String canonicalRequest = String.format("%s\n%s\n%s\n%s\n%s\n%s",
                requestMethod.toUpperCase(),
                canonicalUri,
                canonicalQueryString,
                canonicalHeaders,
                signedHeaders,
                contentSha256);
        log.info("canonical request: \n{}", canonicalRequest);
        String hashedCanonicalRequest = S3SignatureUtils.sha256Hex(canonicalRequest);
        String credentialScope = String.format("%s/%s/%s/%s", dateStamp, region, SERVICE, AWS4_REQUEST);
        String stringToSign = String.format("%s\n%s\n%s\n%s",
                ALGORITHM,
                dateTime,
                credentialScope,
                hashedCanonicalRequest);
        log.info("string to sign: \n{}", stringToSign);
        byte[] signingKey = S3SignatureUtils.buildSigningKey(secretKey, dateStamp, region);
        String signature = HexFormat.of().formatHex(HMACUtils.hmacSha256(signingKey, stringToSign));
        String authorization = String.format("%s Credential=%s/%s, SignedHeaders=%s, Signature=%s",
                ALGORITHM, accessKey, credentialScope, signedHeaders, signature);
        log.info("authorization: {}", authorization);
        return authorization;
    }

    public static String sha256Hex(String canonicalRequest) {
        return DigestUtils.sha256Hex(canonicalRequest).toLowerCase();
    }

    public static String sha256Hex(byte[] data) {
        return DigestUtils.sha256Hex(data).toLowerCase();
    }

    public static String sha256Hex(Path filePath) throws NoSuchAlgorithmException, IOException {
        return DigestUtils.getFileDigest(filePath, "SHA256").toLowerCase();
    }

    public static String buildCanonicalQueryString(Map<String, String> params) {
        return params.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .map(e -> urlEncode(e.getKey()) + "=" + urlEncode(e.getValue()))
                .collect(Collectors.joining("&"));
    }

    public static String buildCanonicalHeaders(Map<String, String> headers) {
        return headers.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .map(e -> e.getKey().toLowerCase() + ":" + e.getValue().trim() + "\n")
                .collect(Collectors.joining());
    }

    public static String buildSignedHeaders(Map<String, String> headers) {
        return headers.keySet().stream()
                .map(String::toLowerCase)
                .sorted()
                .collect(Collectors.joining(";"));
    }

    public static String urlEncodePath(String key) {
        return Arrays.stream(key.split("/", -1))
                .map(S3SignatureUtils::urlEncode)
                .collect(Collectors.joining("/"));
    }

    public static String urlEncode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8)
                // 空格编码后变成了+，需要改成%20
                .replace("+", "%20")
                // *编码后还是*，需要改成%2A
                .replace("*", "%2A")
                // ~编码后变成了%7E，需要改回来
                .replace("%7E", "~");
    }


    public static byte[] buildSigningKey(String secretKey, String dateStamp, String region) {
        byte[] kSecret = ("AWS4" + secretKey).getBytes(StandardCharsets.UTF_8);
        byte[] kDate = HMACUtils.hmacSha256(kSecret, dateStamp);
        byte[] kRegion = HMACUtils.hmacSha256(kDate, region);
        byte[] kService = HMACUtils.hmacSha256(kRegion, SERVICE);
        return HMACUtils.hmacSha256(kService, AWS4_REQUEST);
    }

    public static void main(String[] args) {
        System.out.println(URLEncoder.encode(" ", StandardCharsets.UTF_8));
        System.out.println(URLEncoder.encode("*", StandardCharsets.UTF_8));
        System.out.println(URLEncoder.encode("~", StandardCharsets.UTF_8));
        System.out.println(URLDecoder.decode("%E6%B5%8B%E8%AF%95%20%E7%9B%AE%E5%BD%95%2F", StandardCharsets.UTF_8));
        System.out.println(URI.create("www.baidu.com").getHost());
    }
}
