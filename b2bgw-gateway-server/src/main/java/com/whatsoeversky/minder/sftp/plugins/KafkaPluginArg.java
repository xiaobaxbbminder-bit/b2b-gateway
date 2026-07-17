package com.whatsoeversky.minder.sftp.plugins;

import lombok.Data;

@Data
public class KafkaPluginArg {
    private String bootstrapServers;
    private String topic;
    private String key;
    private String messageTemplate;
}
