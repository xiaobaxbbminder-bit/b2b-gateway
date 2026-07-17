package com.whatsoeversky.minder.sftp.plugins;

import lombok.Data;

@Data
public class RocketMqPluginArg {
    private String nameServer;
    private String topic;
    private String producerGroup;
    private String tag;
    private String key;
    private String messageTemplate;
}
