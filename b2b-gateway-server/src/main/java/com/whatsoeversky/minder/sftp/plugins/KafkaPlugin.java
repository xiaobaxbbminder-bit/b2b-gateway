package com.whatsoeversky.minder.sftp.plugins;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.whatsoeversky.minder.sftp.support.FileRunContext;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Path;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

@Slf4j
@Component
public class KafkaPlugin implements Plugin {

    private static final DateTimeFormatter DTF = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
            .withZone(ZoneId.systemDefault());

    @Resource
    private ObjectMapper objectMapper;

    @Override
    public String getPluginName() {
        return "kafka";
    }

    @Override
    public void execute(FileRunContext context, String args) throws IOException {
        KafkaPluginArg arg = objectMapper.readValue(args, KafkaPluginArg.class);
        Path file = context.getFile();

        String message = resolveTemplate(arg.getMessageTemplate(), file);
        String key = resolveTemplate(arg.getKey(), file);

        Properties props = new Properties();
        props.put("bootstrap.servers", arg.getBootstrapServers());
        props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");

        try (KafkaProducer<String, String> producer = new KafkaProducer<>(props)) {
            ProducerRecord<String, String> record = new ProducerRecord<>(arg.getTopic(), key, message);
            producer.send(record, (metadata, exception) -> {
                if (exception != null) {
                    log.error("kafka send error, topic: {}, key: {}", arg.getTopic(), key, exception);
                } else {
                    log.info("kafka send success, topic: {}, partition: {}, offset: {}",
                            metadata.topic(), metadata.partition(), metadata.offset());
                }
            });
            producer.flush();
        } catch (Exception e) {
            log.error("kafka producer error", e);
            throw new IOException("kafka send failed: " + e.getMessage());
        }

        Map<String, Object> result = new HashMap<>();
        result.put("topic", arg.getTopic());
        result.put("key", key);
        result.put("message", message);
        context.putContextVariables(getPluginName(),
                FileRunContext.ContextVariable.builder().args(arg).res(result).build());
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
