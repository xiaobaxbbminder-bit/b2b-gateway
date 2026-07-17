package com.whatsoeversky.minder.helper;

import com.whatsoeversky.minder.helper.s3.*;
import com.whatsoeversky.minder.utils.HttpClientUtils;
import com.whatsoeversky.minder.utils.S3SignatureUtils;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import kotlin.collections.MapsKt;
import org.apache.commons.io.IOUtils;
import org.apache.hc.client5.http.classic.methods.HttpDelete;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.classic.methods.HttpHead;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.classic.methods.HttpPut;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManager;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.http.io.entity.InputStreamEntity;
import org.apache.hc.core5.net.URIBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilderFactory;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component
public class S3ClientHelper {


    @Value("${s3.http.pool.maxTotal:10}")
    private int maxTotal;
    @Value("${s3.http.pool.maxPerRoute:2}")
    private int maxPerRoute;
    @Value("${s3.http.pool.soTimeoutSeconds:10}")
    private int soTimeoutSeconds;
    @Value("${s3.http.pool.connectTimeoutSeconds:10}")
    private int connectTimeoutSeconds;
    @Value("${s3.http.pool.socketTimeoutSeconds:10}")
    private int socketTimeoutSeconds;

    @Value("${s3.http.pool.connectionRequestTimeoutSeconds:10}")
    private int connectionRequestTimeoutSeconds;
    @Value("${s3.http.pool.responseTimeoutSeconds:10}")
    private int responseTimeoutSeconds;

    private PoolingHttpClientConnectionManager poolingHttpClientConnectionManager;

    private CloseableHttpClient httpClient;


    @PostConstruct
    public void init() {
        poolingHttpClientConnectionManager = HttpClientUtils.createPoolingHttpClientConnectionManager(maxTotal, 
            maxPerRoute, 
            connectTimeoutSeconds,
            socketTimeoutSeconds,
            soTimeoutSeconds);
        httpClient = HttpClientUtils.getPoolingHttpClient(poolingHttpClientConnectionManager,
            connectionRequestTimeoutSeconds,
            responseTimeoutSeconds
        );
    }

    public S3ListObjectResponse listObject(S3ListObjectRequest request) throws IOException, URISyntaxException {
        String bucket = request.getBucket();
        String endpoint = request.getEndpoint();
        boolean pathStyle = request.isPathStyle();
        String canonicalUri = getCanonicalUri(pathStyle, bucket);

        Map<String, String> queryParams = new LinkedHashMap<>();
        if (request.getPrefix() != null) queryParams.put("prefix", request.getPrefix());
        if (request.getDelimiter() != null) queryParams.put("delimiter", request.getDelimiter());
        if (request.getMarker() != null) queryParams.put("marker", request.getMarker());
        if (request.getMaxKeys() != null) queryParams.put("max-keys", request.getMaxKeys().toString());

        Instant now = Instant.now();
        String dateTime = DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss'Z'").withZone(ZoneOffset.UTC).format(now);
        String dateStamp = DateTimeFormatter.ofPattern("yyyyMMdd").withZone(ZoneOffset.UTC).format(now);

        Map<String, String> headers = new LinkedHashMap<>();
        headers.put("host", endpoint);
        headers.put("x-amz-date", dateTime);
        headers.put("x-amz-content-sha256", S3SignatureUtils.EMPTY_PAYLOAD_SHA256);

        headers.put("Authorization", S3SignatureUtils.sign("GET",
                canonicalUri, queryParams, headers, request.getRegion(), dateStamp,
                dateTime, S3SignatureUtils.EMPTY_PAYLOAD_SHA256, request.getAccessKey(), request.getSecretKey()));

        URIBuilder uriBuilder = getUriBuilder(endpoint, request.isUseHttps())
                .setPath(canonicalUri);
        queryParams.forEach(uriBuilder::addParameter);
        HttpGet httpGet = new HttpGet(uriBuilder.build());
        for (Map.Entry<String, String> entry : headers.entrySet()) {
            httpGet.setHeader(entry.getKey(), entry.getValue());
        }
        return httpClient.execute(httpGet, response -> {
            int statusCode = response.getCode();
            if (statusCode != 200) {
                String errorXml = EntityUtils.toString(response.getEntity());
                throw new RuntimeException("S3 list objects failed, status: " + statusCode + ", response: " + errorXml);
            }
            String xml = EntityUtils.toString(response.getEntity());
            return parseListObjectsResponse(xml);
        });
    }

    private String getCanonicalUri(boolean pathStyle, String bucket) {
        if (pathStyle) {
            return "/" + bucket + "/";
        }
        return "/";
    }

    private String getCanonicalUri(boolean pathStyle, String bucket, String key) {
        if (pathStyle) {
            return "/" + bucket + "/" + key;
        }
        return "/" + key;
    }


    private static S3ListObjectResponse parseListObjectsResponse(String xml) {
        try {
            Document document = DocumentBuilderFactory.newInstance().newDocumentBuilder()
                    .parse(new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8)));
            Element root = document.getDocumentElement();

            S3ListObjectResponse.S3ListObjectResponseBuilder builder = S3ListObjectResponse.builder();
            builder.name(getElementText(root, "Name"));
            builder.prefix(getElementText(root, "Prefix"));
            builder.delimiter(getElementText(root, "Delimiter"));
            builder.marker(getElementText(root, "Marker"));

            String maxKeys = getElementText(root, "MaxKeys");
            if (maxKeys != null) builder.maxKeys(Integer.parseInt(maxKeys));
            String isTruncated = getElementText(root, "IsTruncated");
            if (isTruncated != null) builder.isTruncated(Boolean.parseBoolean(isTruncated));

            List<String> commonPrefixes = new ArrayList<>();
            NodeList commonPrefixesList = root.getElementsByTagName("CommonPrefixes");
            for (int i = 0; i < commonPrefixesList.getLength(); i++) {
                String prefix = getElementText((Element) commonPrefixesList.item(i), "Prefix");
                if (prefix != null) commonPrefixes.add(prefix);
            }
            builder.commonPrefixes(commonPrefixes);

            List<S3ListObjectResponse.Item> items = new ArrayList<>();
            NodeList contentsList = root.getElementsByTagName("Contents");
            for (int i = 0; i < contentsList.getLength(); i++) {
                Element element = (Element) contentsList.item(i);
                String key = getElementText(element, "Key");
                String sizeStr = getElementText(element, "Size");
                long size = sizeStr != null ? Long.parseLong(sizeStr) : 0;
                String etag = getElementText(element, "ETag");
                String lastModified = getElementText(element, "LastModified");
                Instant lastModifiedInstant = lastModified != null ? Instant.parse(lastModified) : null;
                items.add(new S3ListObjectResponse.Item(key, size, etag, lastModified, lastModifiedInstant));
            }
            builder.items(items);

            return builder.build();
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse S3 list objects response", e);
        }
    }

    private static String getElementText(Element parent, String tagName) {
        NodeList list = parent.getElementsByTagName(tagName);
        if (list.getLength() > 0) {
            String text = list.item(0).getTextContent();
            return text != null ? text.trim() : null;
        }
        return null;
    }

    public S3PutObjectResponse putObject(S3PutObjectRequest request) throws IOException, URISyntaxException, NoSuchAlgorithmException {
        InputStream inputStream = null;
        byte[] data = request.getData();
        Path filePath = request.getFilePath();
        String contentSha256;
        long contentLength;
        try {
            if (data != null) {
                inputStream = new ByteArrayInputStream(data);
                // 从data计算sha256值
                contentSha256 = S3SignatureUtils.sha256Hex(data);
                contentLength = data.length;
            } else {
                inputStream = Files.newInputStream(filePath);
                // 从filePath计算sha256值
                contentSha256 = S3SignatureUtils.sha256Hex(filePath);
                contentLength = Files.size(filePath);
            }
            String bucket = request.getBucket();
            String endpoint = request.getEndpoint();
            boolean pathStyle = request.isPathStyle();
            String key = S3SignatureUtils.urlEncodePath(request.getKey());
            String contentType = request.getContentType();
            String canonicalUri = getCanonicalUri(pathStyle, bucket, key);
            String effectiveContentType = (contentType != null && !contentType.isEmpty()) ? contentType : "application/octet-stream";

            Instant now = Instant.now();
            String dateTime = DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss'Z'").withZone(ZoneOffset.UTC).format(now);
            String dateStamp = DateTimeFormatter.ofPattern("yyyyMMdd").withZone(ZoneOffset.UTC).format(now);

            Map<String, String> headers = new LinkedHashMap<>();
            headers.put("host", endpoint);
            headers.put("x-amz-date", dateTime);
            headers.put("x-amz-content-sha256", contentSha256);
            // headers.put("content-type", effectiveContentType);
            // headers.put("content-length", String.valueOf(contentLength));
            headers.put("Authorization", S3SignatureUtils.sign("PUT",
                    canonicalUri, MapsKt.emptyMap(), headers, request.getRegion(),
                    dateStamp, dateTime, contentSha256, request.getAccessKey(), request.getSecretKey()));
            URIBuilder uriBuilder = getUriBuilder(endpoint, request.isUseHttps())
                    .setPath(pathStyle ? "/" + bucket + "/" + request.getKey() : "/" + request.getKey());
            HttpPut httpPut = new HttpPut(uriBuilder.build());
            httpPut.setEntity(new InputStreamEntity(inputStream, contentLength, ContentType.create(effectiveContentType)));
            //httpPut.setEntity(new InputStreamEntity(inputStream, null));
            for (Map.Entry<String, String> entry : headers.entrySet()) {
                httpPut.setHeader(entry.getKey(), entry.getValue());
            }
            return httpClient.execute(httpPut, response -> {
                int statusCode = response.getCode();
                if (statusCode != 200) {
                    String errorXml = EntityUtils.toString(response.getEntity());
                    throw new RuntimeException("S3 put object failed, status: " + statusCode + ", response: " + errorXml);
                }
                String etag = response.getHeader("ETag") != null ? response.getHeader("ETag").getValue() : null;
                return S3PutObjectResponse.builder().etag(etag).build();
            });
        } finally {
            IOUtils.closeQuietly(inputStream);
        }
    }


    private URIBuilder getUriBuilder(String endpoint, boolean useHttps) {
        int splitIndex;
        String host;
        int port;
        if ((splitIndex = endpoint.indexOf(":")) > 0) {
            host = endpoint.substring(0, splitIndex);
            port = Integer.parseInt(endpoint.substring(splitIndex + 1));
        } else {
            host = endpoint;
            port = useHttps ? 443 : 80;
        }
        return new URIBuilder()
                .setScheme(useHttps ? "https" : "http")
                .setHost(host)
                .setPort(port);
    }


    public S3GetObjectResponse getObject(S3GetObjectRequest request) throws IOException, URISyntaxException {
        String bucket = request.getBucket();
        String endpoint = request.getEndpoint();
        boolean pathStyle = request.isPathStyle();
        String key = S3SignatureUtils.urlEncodePath(request.getKey());
        String canonicalUri = getCanonicalUri(pathStyle, bucket, key);

        Instant now = Instant.now();
        String dateTime = DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss'Z'").withZone(ZoneOffset.UTC).format(now);
        String dateStamp = DateTimeFormatter.ofPattern("yyyyMMdd").withZone(ZoneOffset.UTC).format(now);

        String contentSha256 = S3SignatureUtils.EMPTY_PAYLOAD_SHA256;
        Map<String, String> headers = new LinkedHashMap<>();
        headers.put("host", endpoint);
        headers.put("x-amz-date", dateTime);
        headers.put("x-amz-content-sha256", contentSha256);

        headers.put("Authorization", S3SignatureUtils.sign("GET", canonicalUri, MapsKt.emptyMap(),
                headers, request.getRegion(), dateStamp, dateTime, contentSha256, request.getAccessKey(), request.getSecretKey()));

        URIBuilder uriBuilder = getUriBuilder(endpoint, request.isUseHttps())
                .setPath(pathStyle ? "/" + bucket + "/" + request.getKey() : "/" + request.getKey());
        HttpGet httpGet = new HttpGet(uriBuilder.build());
        for (Map.Entry<String, String> entry : headers.entrySet()) {
            httpGet.setHeader(entry.getKey(), entry.getValue());
        }

        return httpClient.execute(httpGet, response -> {
            int statusCode = response.getCode();
            if (statusCode != 200) {
                String errorXml = EntityUtils.toString(response.getEntity());
                throw new RuntimeException("S3 get object failed, status: " + statusCode + ", response: " + errorXml);
            }
            byte[] data = EntityUtils.toByteArray(response.getEntity());
            String etag = response.getHeader("ETag") != null ? response.getHeader("ETag").getValue() : null;
            String respContentType = response.getHeader("Content-Type") != null ? response.getHeader("Content-Type").getValue() : null;
            long contentLength = response.getEntity().getContentLength();
            return S3GetObjectResponse.builder()
                    .data(data)
                    .etag(etag)
                    .contentType(respContentType)
                    .contentLength(contentLength)
                    .build();
        });
    }

    public S3HeadObjectResponse headObject(S3HeadObjectRequest request) throws IOException, URISyntaxException {
        String bucket = request.getBucket();
        String endpoint = request.getEndpoint();
        boolean pathStyle = request.isPathStyle();
        String key = S3SignatureUtils.urlEncodePath(request.getKey());
        String canonicalUri = getCanonicalUri(pathStyle, bucket, key);

        Instant now = Instant.now();
        String dateTime = DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss'Z'").withZone(ZoneOffset.UTC).format(now);
        String dateStamp = DateTimeFormatter.ofPattern("yyyyMMdd").withZone(ZoneOffset.UTC).format(now);

        String contentSha256 = S3SignatureUtils.EMPTY_PAYLOAD_SHA256;
        Map<String, String> headers = new LinkedHashMap<>();
        headers.put("host", endpoint);
        headers.put("x-amz-date", dateTime);
        headers.put("x-amz-content-sha256", contentSha256);

        headers.put("Authorization", S3SignatureUtils.sign("HEAD", canonicalUri, MapsKt.emptyMap(),
                headers, request.getRegion(), dateStamp, dateTime, contentSha256, request.getAccessKey(), request.getSecretKey()));

        URIBuilder uriBuilder = getUriBuilder(endpoint, request.isUseHttps())
                .setPath(pathStyle ? "/" + bucket + "/" + request.getKey() : "/" + request.getKey());
        HttpHead httpHead = new HttpHead(uriBuilder.build());
        for (Map.Entry<String, String> entry : headers.entrySet()) {
            httpHead.setHeader(entry.getKey(), entry.getValue());
        }

        return httpClient.execute(httpHead, response -> {
            int statusCode = response.getCode();
            if (statusCode != 200) {
                String errorXml = EntityUtils.toString(response.getEntity());
                throw new RuntimeException("S3 head object failed, status: " + statusCode + ", response: " + errorXml);
            }
            String etag = response.getHeader("ETag") != null ? response.getHeader("ETag").getValue() : null;
            String contentType = response.getHeader("Content-Type") != null ? response.getHeader("Content-Type").getValue() : null;
            String lastModified = response.getHeader("Last-Modified") != null ? response.getHeader("Last-Modified").getValue() : null;
            Instant lastModifiedInstant = lastModified != null ? Instant.from(DateTimeFormatter.RFC_1123_DATE_TIME.parse(lastModified)) : null;
            String versionId = response.getHeader("x-amz-version-id") != null ? response.getHeader("x-amz-version-id").getValue() : null;
            String storageClass = response.getHeader("x-amz-storage-class") != null ? response.getHeader("x-amz-storage-class").getValue() : null;
            String serverSideEncryption = response.getHeader("x-amz-server-side-encryption") != null ? response.getHeader("x-amz-server-side-encryption").getValue() : null;
            long contentLength = response.getHeader("Content-Length") != null
                    ? Long.parseLong(response.getHeader("Content-Length").getValue())
                    : 0;

            Map<String, String> userMetadata = new LinkedHashMap<>();
            for (org.apache.hc.core5.http.Header header : response.getHeaders()) {
                String name = header.getName();
                if (name.startsWith("x-amz-meta-")) {
                    userMetadata.put(name.substring("x-amz-meta-".length()), header.getValue());
                }
            }

            return S3HeadObjectResponse.builder()
                    .etag(etag)
                    .contentType(contentType)
                    .contentLength(contentLength)
                    .lastModified(lastModified)
                    .lastModifiedInstant(lastModifiedInstant)
                    .versionId(versionId)
                    .storageClass(storageClass)
                    .serverSideEncryption(serverSideEncryption)
                    .userMetadata(userMetadata)
                    .build();
        });
    }

    public S3CreateMultipartUploadResponse createMultipartUpload(S3CreateMultipartUploadRequest request) throws IOException, URISyntaxException {
        String bucket = request.getBucket();
        String endpoint = request.getEndpoint();
        boolean pathStyle = request.isPathStyle();
        String key = S3SignatureUtils.urlEncodePath(request.getKey());
        String canonicalUri = getCanonicalUri(pathStyle, bucket, key);

        Map<String, String> queryParams = new LinkedHashMap<>();
        queryParams.put("uploads", "");

        Instant now = Instant.now();
        String dateTime = DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss'Z'").withZone(ZoneOffset.UTC).format(now);
        String dateStamp = DateTimeFormatter.ofPattern("yyyyMMdd").withZone(ZoneOffset.UTC).format(now);

        String contentSha256 = S3SignatureUtils.EMPTY_PAYLOAD_SHA256;
        Map<String, String> headers = new LinkedHashMap<>();
        headers.put("host", endpoint);
        headers.put("x-amz-date", dateTime);
        headers.put("x-amz-content-sha256", contentSha256);

        headers.put("Authorization", S3SignatureUtils.sign("POST", canonicalUri, queryParams, headers,
                request.getRegion(), dateStamp, dateTime, contentSha256,
                request.getAccessKey(), request.getSecretKey()));

        URIBuilder uriBuilder = getUriBuilder(endpoint, request.isUseHttps())
                .setPath(pathStyle ? "/" + bucket + "/" + request.getKey() : "/" + request.getKey());
        queryParams.forEach(uriBuilder::addParameter);
        HttpPost httpPost = new HttpPost(uriBuilder.build());
        for (Map.Entry<String, String> entry : headers.entrySet()) {
            httpPost.setHeader(entry.getKey(), entry.getValue());
        }

        return httpClient.execute(httpPost, response -> {
            int statusCode = response.getCode();
            if (statusCode != 200) {
                String errorXml = EntityUtils.toString(response.getEntity());
                throw new RuntimeException("S3 create multipart upload failed, status: " + statusCode + ", response: " + errorXml);
            }
            String xml = EntityUtils.toString(response.getEntity());
            try {
                Document document = DocumentBuilderFactory.newInstance().newDocumentBuilder()
                        .parse(new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8)));
                Element root = document.getDocumentElement();
                return S3CreateMultipartUploadResponse.builder()
                        .uploadId(getElementText(root, "UploadId"))
                        .bucket(getElementText(root, "Bucket"))
                        .key(getElementText(root, "Key"))
                        .build();
            } catch (Exception e) {
                throw new RuntimeException("Failed to parse S3 create multipart upload response", e);
            }
        });
    }

    public S3UploadPartResponse uploadPart(S3UploadPartRequest request) throws IOException, URISyntaxException, NoSuchAlgorithmException {
        String bucket = request.getBucket();
        String endpoint = request.getEndpoint();
        boolean pathStyle = request.isPathStyle();
        String key = S3SignatureUtils.urlEncodePath(request.getKey());
        String canonicalUri = getCanonicalUri(pathStyle, bucket, key);

        Map<String, String> queryParams = new LinkedHashMap<>();
        queryParams.put("partNumber", String.valueOf(request.getPartNumber()));
        queryParams.put("uploadId", request.getUploadId());

        byte[] partData;
        try (RandomAccessFile raf = new RandomAccessFile(request.getFilePath().toFile(), "r")) {
            raf.seek(request.getFileOffset());
            int partSize = request.getPartSize();
            partData = new byte[partSize];
            int bytesRead = raf.read(partData);
            if (bytesRead < partSize) {
                byte[] trimmed = new byte[bytesRead];
                System.arraycopy(partData, 0, trimmed, 0, bytesRead);
                partData = trimmed;
            }
        }
        String contentSha256 = S3SignatureUtils.sha256Hex(partData);
        long contentLength = partData.length;

        Instant now = Instant.now();
        String dateTime = DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss'Z'").withZone(ZoneOffset.UTC).format(now);
        String dateStamp = DateTimeFormatter.ofPattern("yyyyMMdd").withZone(ZoneOffset.UTC).format(now);

        Map<String, String> headers = new LinkedHashMap<>();
        headers.put("host", endpoint);
        headers.put("x-amz-date", dateTime);
        headers.put("x-amz-content-sha256", contentSha256);

        headers.put("Authorization", S3SignatureUtils.sign("PUT", canonicalUri, queryParams, headers,
                request.getRegion(), dateStamp, dateTime, contentSha256,
                request.getAccessKey(), request.getSecretKey()));

        URIBuilder uriBuilder = getUriBuilder(endpoint, request.isUseHttps())
                .setPath(pathStyle ? "/" + bucket + "/" + request.getKey() : "/" + request.getKey());
        queryParams.forEach(uriBuilder::addParameter);
        HttpPut httpPut = new HttpPut(uriBuilder.build());
        httpPut.setEntity(new InputStreamEntity(new ByteArrayInputStream(partData), contentLength, ContentType.APPLICATION_OCTET_STREAM));
        for (Map.Entry<String, String> entry : headers.entrySet()) {
            httpPut.setHeader(entry.getKey(), entry.getValue());
        }

        return httpClient.execute(httpPut, response -> {
            int statusCode = response.getCode();
            if (statusCode != 200) {
                String errorXml = EntityUtils.toString(response.getEntity());
                throw new RuntimeException("S3 upload part failed, status: " + statusCode + ", response: " + errorXml);
            }
            String etag = response.getHeader("ETag") != null ? response.getHeader("ETag").getValue() : null;
            return S3UploadPartResponse.builder()
                    .partNumber(request.getPartNumber())
                    .etag(etag)
                    .build();
        });
    }

    public S3CompleteMultipartUploadResponse completeMultipartUpload(S3CompleteMultipartUploadRequest request) throws IOException, URISyntaxException, NoSuchAlgorithmException {
        String bucket = request.getBucket();
        String endpoint = request.getEndpoint();
        boolean pathStyle = request.isPathStyle();
        String key = S3SignatureUtils.urlEncodePath(request.getKey());
        String canonicalUri = getCanonicalUri(pathStyle, bucket, key);

        Map<String, String> queryParams = new LinkedHashMap<>();
        queryParams.put("uploadId", request.getUploadId());

        StringBuilder xmlBuilder = new StringBuilder();
        xmlBuilder.append("<CompleteMultipartUpload>");
        for (S3CompleteMultipartUploadRequest.PartInfo part : request.getParts()) {
            xmlBuilder.append("<Part>")
                    .append("<PartNumber>").append(part.getPartNumber()).append("</PartNumber>")
                    .append("<ETag>").append(part.getEtag()).append("</ETag>")
                    .append("</Part>");
        }
        xmlBuilder.append("</CompleteMultipartUpload>");
        String xmlBody = xmlBuilder.toString();
        String contentSha256 = S3SignatureUtils.sha256Hex(xmlBody);

        Instant now = Instant.now();
        String dateTime = DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss'Z'").withZone(ZoneOffset.UTC).format(now);
        String dateStamp = DateTimeFormatter.ofPattern("yyyyMMdd").withZone(ZoneOffset.UTC).format(now);

        Map<String, String> headers = new LinkedHashMap<>();
        headers.put("host", endpoint);
        headers.put("x-amz-date", dateTime);
        headers.put("x-amz-content-sha256", contentSha256);

        headers.put("Authorization", S3SignatureUtils.sign("POST", canonicalUri, queryParams, headers,
                request.getRegion(), dateStamp, dateTime, contentSha256,
                request.getAccessKey(), request.getSecretKey()));

        URIBuilder uriBuilder = getUriBuilder(endpoint, request.isUseHttps())
                .setPath(pathStyle ? "/" + bucket + "/" + request.getKey() : "/" + request.getKey());
        queryParams.forEach(uriBuilder::addParameter);
        HttpPost httpPost = new HttpPost(uriBuilder.build());
        httpPost.setEntity(new StringEntity(xmlBody, ContentType.APPLICATION_XML));
        for (Map.Entry<String, String> entry : headers.entrySet()) {
            httpPost.setHeader(entry.getKey(), entry.getValue());
        }

        return httpClient.execute(httpPost, response -> {
            int statusCode = response.getCode();
            if (statusCode != 200) {
                String errorXml = EntityUtils.toString(response.getEntity());
                throw new RuntimeException("S3 complete multipart upload failed, status: " + statusCode + ", response: " + errorXml);
            }
            String xml = EntityUtils.toString(response.getEntity());
            try {
                Document document = DocumentBuilderFactory.newInstance().newDocumentBuilder()
                        .parse(new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8)));
                Element root = document.getDocumentElement();
                return S3CompleteMultipartUploadResponse.builder()
                        .location(getElementText(root, "Location"))
                        .bucket(getElementText(root, "Bucket"))
                        .key(getElementText(root, "Key"))
                        .etag(getElementText(root, "ETag"))
                        .build();
            } catch (Exception e) {
                throw new RuntimeException("Failed to parse S3 complete multipart upload response", e);
            }
        });
    }

    public void abortMultipartUpload(S3AbortMultipartUploadRequest request) throws IOException, URISyntaxException {
        String bucket = request.getBucket();
        String endpoint = request.getEndpoint();
        boolean pathStyle = request.isPathStyle();
        String key = S3SignatureUtils.urlEncodePath(request.getKey());
        String canonicalUri = getCanonicalUri(pathStyle, bucket, key);

        Map<String, String> queryParams = new LinkedHashMap<>();
        queryParams.put("uploadId", request.getUploadId());

        Instant now = Instant.now();
        String dateTime = DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss'Z'").withZone(ZoneOffset.UTC).format(now);
        String dateStamp = DateTimeFormatter.ofPattern("yyyyMMdd").withZone(ZoneOffset.UTC).format(now);

        String contentSha256 = S3SignatureUtils.EMPTY_PAYLOAD_SHA256;
        Map<String, String> headers = new LinkedHashMap<>();
        headers.put("host", endpoint);
        headers.put("x-amz-date", dateTime);
        headers.put("x-amz-content-sha256", contentSha256);

        headers.put("Authorization", S3SignatureUtils.sign("DELETE", canonicalUri, queryParams, headers,
                request.getRegion(), dateStamp, dateTime, contentSha256,
                request.getAccessKey(), request.getSecretKey()));

        URIBuilder uriBuilder = getUriBuilder(endpoint, request.isUseHttps())
                .setPath(pathStyle ? "/" + bucket + "/" + request.getKey() : "/" + request.getKey());
        queryParams.forEach(uriBuilder::addParameter);
        HttpDelete httpDelete = new HttpDelete(uriBuilder.build());
        for (Map.Entry<String, String> entry : headers.entrySet()) {
            httpDelete.setHeader(entry.getKey(), entry.getValue());
        }

        httpClient.execute(httpDelete, response -> {
            int statusCode = response.getCode();
            if (statusCode != 204 && statusCode != 200) {
                String errorXml = EntityUtils.toString(response.getEntity());
                throw new RuntimeException("S3 abort multipart upload failed, status: " + statusCode + ", response: " + errorXml);
            }
            return null;
        });
    }

    public S3CompleteMultipartUploadResponse multipartUpload(S3MultipartUploadRequest request) throws IOException, URISyntaxException, NoSuchAlgorithmException {
        String bucket = request.getBucket();
        String key = request.getKey();
        Path filePath = request.getFilePath();
        long partSize = request.getPartSize() != null ? request.getPartSize() : 5 * 1024 * 1024;

        if (filePath == null) {
            throw new IllegalArgumentException("filePath must be provided");
        }

        S3CreateMultipartUploadRequest createRequest = new S3CreateMultipartUploadRequest();
        copyBaseFields(request, createRequest);
        createRequest.setBucket(bucket);
        createRequest.setKey(key);
        createRequest.setContentType(request.getContentType());

        S3CreateMultipartUploadResponse createResponse = createMultipartUpload(createRequest);
        String uploadId = createResponse.getUploadId();

        List<S3UploadPartResponse> partResponses = new ArrayList<>();
        try {
            long fileSize = Files.size(filePath);
            int partNumber = 1;
            long offset = 0;
            while (offset < fileSize) {
                long currentPartSize = Math.min(partSize, fileSize - offset);

                S3UploadPartRequest partRequest = new S3UploadPartRequest();
                copyBaseFields(request, partRequest);
                partRequest.setBucket(bucket);
                partRequest.setKey(key);
                partRequest.setPartNumber(partNumber);
                partRequest.setUploadId(uploadId);
                partRequest.setFilePath(filePath);
                partRequest.setFileOffset(offset);
                partRequest.setPartSize((int) currentPartSize);

                partResponses.add(uploadPart(partRequest));
                offset += currentPartSize;
                partNumber++;
            }
        } catch (Exception e) {
            S3AbortMultipartUploadRequest abortRequest = new S3AbortMultipartUploadRequest();
            copyBaseFields(request, abortRequest);
            abortRequest.setBucket(bucket);
            abortRequest.setKey(key);
            abortRequest.setUploadId(uploadId);
            abortMultipartUpload(abortRequest);
            throw new RuntimeException("S3 multipart upload failed, aborting", e);
        }

        S3CompleteMultipartUploadRequest completeRequest = new S3CompleteMultipartUploadRequest();
        copyBaseFields(request, completeRequest);
        completeRequest.setBucket(bucket);
        completeRequest.setKey(key);
        completeRequest.setUploadId(uploadId);
        List<S3CompleteMultipartUploadRequest.PartInfo> partInfos = new ArrayList<>();
        for (S3UploadPartResponse pr : partResponses) {
            partInfos.add(new S3CompleteMultipartUploadRequest.PartInfo(pr.getPartNumber(), pr.getEtag()));
        }
        completeRequest.setParts(partInfos);

        return completeMultipartUpload(completeRequest);
    }

    private void copyBaseFields(S3BaseRequest source, S3BaseRequest target) {
        target.setAccessKey(source.getAccessKey());
        target.setSecretKey(source.getSecretKey());
        target.setEndpoint(source.getEndpoint());
        target.setRegion(source.getRegion());
        target.setPathStyle(source.isPathStyle());
        target.setUseHttps(source.isUseHttps());
    }

    @PreDestroy
    public void destroy() {
        IOUtils.closeQuietly(httpClient);
        IOUtils.closeQuietly(poolingHttpClientConnectionManager);
    }
}
