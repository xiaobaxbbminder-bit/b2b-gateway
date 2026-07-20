package com.whatsoeversky.minder.controller;

import com.alibaba.fastjson2.JSON;
import com.whatsoeversky.minder.common.Result;
import com.whatsoeversky.minder.helper.S3ClientHelper;
import com.whatsoeversky.minder.helper.s3.*;
import com.whatsoeversky.minder.sys.repository.SysUserRepository;
import jakarta.annotation.Resource;
import jakarta.servlet.ServletInputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.http.MediaType;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import reactor.core.publisher.Flux;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.security.NoSuchAlgorithmException;
import java.util.Enumeration;

@RestController
@Slf4j
public class TestController {
    @Resource
    private ChatClient.Builder builder;


    @Resource
    private SysUserRepository sysUserRepository;

    @Resource
    private S3ClientHelper s3ClientHelper;


    @GetMapping(value = "/test/test-chat-client", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter testChatClient() {
        SseEmitter sseEmitter = new SseEmitter();
        OpenAiChatOptions openAiChatOptions = OpenAiChatOptions.builder()
                .apiKey("sk-8bdb2db3afe84f73a1668251b786b6a5")
                .baseUrl("https://dashscope.aliyuncs.com/compatible-mode/v1")
                .model("qwen3.7-plus")
                .build();
        ChatModel chatModel = OpenAiChatModel.builder()
                .options(openAiChatOptions)
                .build();
        ChatClient chatClient = ChatClient.builder(chatModel).build();
        Flux<String> flux = chatClient.prompt().user("写一篇800字作文，主题不限").stream().content();
        flux.subscribe(content -> {
            try {
                sseEmitter.send(SseEmitter.event().data(content));
            } catch (IOException e) {
                log.error("error io", e);
                sseEmitter.completeWithError(e);
            }
        }, error -> {
            log.error("error", error);
            sseEmitter.completeWithError(error);
        }, sseEmitter::complete);
        return sseEmitter;
    }

    @GetMapping("/test/test-call")
    public String testCall(@RequestParam("question") String question) {
        ChatClient chatClient = builder
                .build();
        ChatResponse chatResponse = chatClient.prompt(question)
                .tools(new QueryWeatherTools()).call().chatResponse();
        if (chatResponse == null) {
            throw new RuntimeException("null response");
        }
        Generation result = chatResponse.getResult();
        if (result == null) {
            throw new RuntimeException("null result");
        }
        return result.getOutput().getText();
    }


    public static class QueryWeatherTools {
        @Tool(description = "查询某个城市的天气")
        public String queryWeather(@ToolParam(description = "城市名，例如深圳、广州") String city) {
            if (city.equals("深圳")) {
                return "气温35摄氏度，今日有雷暴雨天气";
            } else if (city.equals("长沙")) {
                return "气温25摄氏度，晴";
            } else {
                return "气温0摄氏度，小雪";
            }
        }
    }

    @GetMapping(value = "test/test-sse2", produces = {MediaType.TEXT_EVENT_STREAM_VALUE})
    public SseEmitter testSse2() {
        SseEmitter sseEmitter = new SseEmitter();
        new Thread(() -> {
            try {
                sseEmitter.send("{\"name\":\"jianghui\"}");
                Thread.sleep(5000);
                sseEmitter.send("{\"age\":\"22\"}");
                Thread.sleep(5000);
                sseEmitter.send("{\"salary\":\"300000\"}");
                sseEmitter.complete();
            } catch (Exception e) {
                sseEmitter.completeWithError(e);
            }
        }).start();
        return sseEmitter;
    }

    @GetMapping(value = "test/test-sse", produces = {MediaType.TEXT_EVENT_STREAM_VALUE})
    public SseEmitter testSse(@RequestParam("question") String question) {
        SseEmitter sseEmitter = new SseEmitter();
        ChatClient chatClient = builder.build();
        Flux<ChatResponse> chatResponseFlux = chatClient.prompt(question).tools(new QueryWeatherTools()).stream().chatResponse();
        chatResponseFlux.subscribe(response -> {
                    log.info("response: {}", JSON.toJSONString(response));
                    Generation result = response.getResult();
                    if (result != null) {
                        AssistantMessage output = result.getOutput();
                        String text = output.getText();
                        if (StringUtils.hasLength(text)) {
                            try {
                                sseEmitter.send(text);
                            } catch (IOException e) {
                                sseEmitter.completeWithError(e);
                            }
                        }
                    }
                },
                error -> {
                    log.error("stream error", error);
                    sseEmitter.completeWithError(error);
                },
                () -> {
                    log.info("stream complete");
                    sseEmitter.complete();
                });
        log.info("return sse emitter");
        return sseEmitter;
    }

    @GetMapping("/test/test-chat-client-builder")
    public String testChatClientBuilder() {
        return "success";
    }


    @GetMapping("/test2")
    public String test2(HttpServletResponse response) throws InterruptedException {
        Thread.sleep(8000);
        // response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
        return "test2";
    }

    @PostMapping("/test/test-post")
    public Result<String> testPost(HttpServletRequest request) throws IOException {
        String requestURI = request.getRequestURI();
        log.debug("request uri: {}", requestURI);
        request.getParameterMap().forEach((key, value) -> {
            log.info("request param, name: {}, value: {}", key, String.join(",", value));
        });
        Enumeration<String> headerNames = request.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String name = headerNames.nextElement();
            log.info("request header, name: {}, value: {}", name, request.getHeader(name));
        }
        ServletInputStream inputStream = request.getInputStream();
        log.info("body to string: {}", IOUtils.toString(inputStream, StandardCharsets.UTF_8));
        return Result.success("finish");
    }

    @GetMapping("/test3")
    public S3ListObjectResponse test3() throws IOException, URISyntaxException {
        S3ListObjectRequest s3ListObjectRequest = new S3ListObjectRequest();
        s3ListObjectRequest.setAccessKey("HPUAYJLC3H11SNZAQZ4D");
        s3ListObjectRequest.setSecretKey("F3YE01M6Q2dcAUvId9B0DeVinpg7jKVmWmml3FOO");
        s3ListObjectRequest.setEndpoint("jh-test1.obs.cn-south-1.myhuaweicloud.com");
        s3ListObjectRequest.setBucket("jh-test1");
        s3ListObjectRequest.setRegion("cn-south-1");
        // s3ListObjectRequest.setPrefix("临时目录");
        s3ListObjectRequest.setPrefix("测试 目录/");
//        s3ListObjectRequest.setDelimiter("/");
        s3ListObjectRequest.setPathStyle(false);
        return s3ClientHelper.listObject(s3ListObjectRequest);
    }

    @GetMapping("/test/test-put-object")
    public S3PutObjectResponse test4() throws IOException, URISyntaxException, NoSuchAlgorithmException {
        S3PutObjectRequest s3PutObjectRequest = new S3PutObjectRequest();
        s3PutObjectRequest.setAccessKey("HPUAYJLC3H11SNZAQZ4D");
        s3PutObjectRequest.setSecretKey("F3YE01M6Q2dcAUvId9B0DeVinpg7jKVmWmml3FOO");
        s3PutObjectRequest.setEndpoint("jh-test1.obs.cn-south-1.myhuaweicloud.com");
//        s3PutObjectRequest.setEndpoint("localhost:9030");
        s3PutObjectRequest.setBucket("jh-test1");
        s3PutObjectRequest.setRegion("cn-south-1");

        s3PutObjectRequest.setKey("测试 目录/def/test1.jpg");
        s3PutObjectRequest.setFilePath(Paths.get("/home/whatsoeversky/图片/壁纸/test1.jpg"));

//        Path path = Paths.get("/home/whatsoeversky/图片/壁纸/test1.jpg");
//        byte[] data = Files.readAllBytes(path);
//        s3PutObjectRequest.setKey("测试 目录/def/test1.jpg");
//        s3PutObjectRequest.setData(data);


//        s3PutObjectRequest.setKey("测试 目录/def/test.txt");
//        s3PutObjectRequest.setData("我去你吗的".getBytes(StandardCharsets.UTF_8));

        return s3ClientHelper.putObject(s3PutObjectRequest);
    }

    @GetMapping("/test5")
    public S3GetObjectResponse test5() throws IOException, URISyntaxException {
        S3GetObjectRequest s3GetObjectRequest = new S3GetObjectRequest();
        s3GetObjectRequest.setAccessKey("HPUAYJLC3H11SNZAQZ4D");
        s3GetObjectRequest.setSecretKey("F3YE01M6Q2dcAUvId9B0DeVinpg7jKVmWmml3FOO");
        s3GetObjectRequest.setEndpoint("jh-test1.obs.cn-south-1.myhuaweicloud.com");
        s3GetObjectRequest.setBucket("jh-test1");
        s3GetObjectRequest.setRegion("cn-south-1");
        s3GetObjectRequest.setKey("测试 目录/def/test1.jpg");
        return s3ClientHelper.getObject(s3GetObjectRequest);
    }

    @GetMapping("/test/test-head-object")
    public S3HeadObjectResponse testHeadObject() throws IOException, URISyntaxException {
        S3HeadObjectRequest s3HeadObjectRequest = new S3HeadObjectRequest();
        s3HeadObjectRequest.setAccessKey("HPUAYJLC3H11SNZAQZ4D");
        s3HeadObjectRequest.setSecretKey("F3YE01M6Q2dcAUvId9B0DeVinpg7jKVmWmml3FOO");
        s3HeadObjectRequest.setEndpoint("jh-test1.obs.cn-south-1.myhuaweicloud.com");
        s3HeadObjectRequest.setBucket("jh-test1");
        s3HeadObjectRequest.setRegion("cn-south-1");
        s3HeadObjectRequest.setKey("测试 目录/def/test1.jpg");
        return s3ClientHelper.headObject(s3HeadObjectRequest);
    }

    public static void main(String[] args) {
        System.out.println("-----BEGIN OPENSSH PRIVATE KEY-----\nb3BlbnNzaC1rZXktdjEAAAAABG5vbmUAAAAEbm9uZQAAAAAAAAABAAABFwAAAAdzc2gtcn\nNhAAAAAwEAAQAAAQEAjMtkNPsgKlWRBnr9coKe5aZyl+18yEh3DloW9epXKwTWXdrfTTVL\n6OGUyCQEkGBcnbr5yHOCgKBdqtMm9PM48xeZ/JUK7Hy1wRIseqGX6s7qZsmtmFRG9S0lZA\npTG9JdGEvJ/7GSOILiM075afFfbcwbBisf7TZrVM9mCaDbAuk82qSHeIAXug/EqppIg3IW\nd/vvZ0TgSKHhvmuU3bc5UhN8acRWHshdNnYV8YTjvLWCluSCRHJXzm0pVsJIr6twq7SWPr\nxVSKVXcgcS/5Wb5mo6wZ/m04BdOhfMz8Oo3GyE+l+e+aCDl9qhw+W13E5bhGAY0n/GHmhU\n9tJHXcRknwAAA9CDWMjpg1jI6QAAAAdzc2gtcnNhAAABAQCMy2Q0+yAqVZEGev1ygp7lpn\nKX7XzISHcOWhb16lcrBNZd2t9NNUvo4ZTIJASQYFyduvnIc4KAoF2q0yb08zjzF5n8lQrs\nfLXBEix6oZfqzupmya2YVEb1LSVkClMb0l0YS8n/sZI4guIzTvlp8V9tzBsGKx/tNmtUz2\nYJoNsC6TzapId4gBe6D8SqmkiDchZ3++9nROBIoeG+a5TdtzlSE3xpxFYeyF02dhXxhOO8\ntYKW5IJEclfObSlWwkivq3CrtJY+vFVIpVdyBxL/lZvmajrBn+bTgF06F8zPw6jcbIT6X5\n75oIOX2qHD5bXcTluEYBjSf8YeaFT20kddxGSfAAAAAwEAAQAAAQBGIyY6rGU0Iaca0tcC\nHebZIUdRvulb/plsiV2JyOD+mazOWXPHCwdTLlkHMMYjRBWvL48yF7TJrdmreP+do7JZiw\nbXSEoGUAPc99g3iNtcihJD/TDgww8MIR7QzMHlCLD+ovawTT25sB3OEIf4ClmPIU/PDVmc\nunDScXTz0/F2a4wDe5sYiN/QuhTzKPhGLYZUecWIfbXl75I/E3ILYjeWff2rpBni5OXLxV\nxhGM832CWyiwXRcECIavjjFlRqbStWOnKebQpnLkjipxjpDpC92rvet+k/7gpOYWJ9M5m1\nKdic7FJc3s3vVyOpCdVtw1qs+1AcI+lCGcFhYgg9g1c1AAAAgAoFZxVXrsRu/kLaZuev4s\nns+OmpcW2m1yZiMMn7zwusVe2F25sJg/zGX59p/fpLtXgcHnJo/zIT5jIfXPIWpJeGPf0H\nFhsPsZ/pymJ1QlSepTaLh8pi2C04upHmcFHgSy85irnNDkXM78Tu0P9Wcs1i3kKxT9c3Ga\nwPVAF8dABdAAAAgQDCechD/hAaW5nCuRgnZ7r8qFAmuSrPwXfmxwKQ1foqZNvhFUhiKVGz\n6PK7reu+F3dtnorSzSHOzllkvwYxgAFaYCApBqLkEyWEHV/1KZjLC6nGC5SbPCElpTR34N\nsJ8+tFsC5HoZTgxYYaYfaYlVYM404t0PGBrJm4zZq/5MLQXQAAAIEAuVYR3chrdmg0YtA3\n82hlsSgl35km2spIOhOHGhzebWW754z5ly4hcLHih5BUAOxdTkfsJ6GEH5+I3zmwHulsyx\nD2rQRpduJmOBeitY+DhRGRG7BVW2CNBzyIxfEfhsdC1/1oL2cPb8n/iV5y2FrkfxoH0E+E\nnpCfl+/2zkuWqSsAAAAVd2hhdHNvZXZlcnNreS1NUy03QjIzAQIDBAUG\n-----END OPENSSH PRIVATE KEY-----\n");
    }
}
