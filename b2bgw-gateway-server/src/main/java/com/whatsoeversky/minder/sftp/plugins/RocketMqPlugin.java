package com.whatsoeversky.minder.sftp.plugins;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.whatsoeversky.minder.sftp.support.FileRunContext;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.common.message.Message;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Path;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
public class RocketMqPlugin implements Plugin {

    private static final DateTimeFormatter DTF = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
            .withZone(ZoneId.systemDefault());

    @Resource
    private ObjectMapper objectMapper;

    @Override
    public String getPluginName() {
        return "rocketmq";
    }

    @Override
    public void execute(FileRunContext context, String args) throws IOException {
        RocketMqPluginArg arg = objectMapper.readValue(args, RocketMqPluginArg.class);
        Path file = context.getFile();

        String messageBody = resolveTemplate(arg.getMessageTemplate(), file);
        String messageKey = resolveTemplate(arg.getKey(), file);

        DefaultMQProducer producer = new DefaultMQProducer(arg.getProducerGroup());
        producer.setNamesrvAddr(arg.getNameServer());
        producer.setInstanceName("sftp-rpa-" + Instant.now().toEpochMilli());

        try {
            producer.start();

            Message message = new Message(arg.getTopic(), arg.getTag(), messageKey, messageBody.getBytes());
            org.apache.rocketmq.client.producer.SendResult sendResult = producer.send(message);

            log.info("rocketmq send success, topic: {}, msgId: {}, queue: {}",
                    sendResult.getMessageQueue().getTopic(),
                    sendResult.getMsgId(),
                    sendResult.getMessageQueue().getQueueId());

            Map<String, Object> result = new HashMap<>();
            result.put("topic", arg.getTopic());
            result.put("tag", arg.getTag());
            result.put("key", messageKey);
            result.put("messageId", sendResult.getMsgId());
            result.put("message", messageBody);
            context.putContextVariables(getPluginName(),
                    FileRunContext.ContextVariable.builder().args(arg).res(result).build());
        } catch (Exception e) {
            log.error("rocketmq send error", e);
            throw new IOException("rocketmq send failed: " + e.getMessage());
        } finally {
            producer.shutdown();
        }
    }

    private String resolveTemplate(String template, Path file) {
        if (template == null || template.isBlank()) {
            return "";
        }
        String filename = file.getFileName().toString();
        String filepath = file.toAbsolutePath().normalize().toString();
        long filesize = file.toFile().length();
        String timestamp = DTF.format(Instant.now());

        return template
                .replace("{filename}", filename)
                .replace("{filepath}", filepath)
                .replace("{filesize}", String.valueOf(filesize))
                .replace("{timestamp}", timestamp);
    }
}
