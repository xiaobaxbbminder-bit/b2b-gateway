package com.whatsoeversky.minder.sftp.runner;

import com.whatsoeversky.minder.sftp.entity.SftpPluginDefinition;
import com.whatsoeversky.minder.sftp.entity.SftpPluginDefinition.VisibilityRule;
import com.whatsoeversky.minder.sftp.repository.SftpPluginDefinitionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class PluginInitRunner implements ApplicationRunner {

    @Autowired
    private SftpPluginDefinitionRepository sftpPluginDefinitionRepository;

    @Override
    public void run(ApplicationArguments args) {
        if (sftpPluginDefinitionRepository.count() > 0) {
            return;
        }

        sftpPluginDefinitionRepository.save(SftpPluginDefinition.builder()
                .name("checksum")
                .label("Checksum 校验")
                .description("对传输的文件进行哈希校验，确保文件完整性")
                .visibility(List.of(
                        VisibilityRule.builder().userType("server").triggerType("file_upload").invokeMode(null)
                                .execNodes(List.of("before_upload", "after_upload")).build(),
                        VisibilityRule.builder().userType("server").triggerType("file_download").invokeMode(null)
                                .execNodes(List.of("before_download", "after_download")).build(),
                        VisibilityRule.builder().userType("client").triggerType(null).invokeMode("sftp_download")
                                .execNodes(List.of("before_download", "after_download")).build(),
                        VisibilityRule.builder().userType("client").triggerType(null).invokeMode("sftp_upload")
                                .execNodes(List.of("before_upload", "after_upload")).build(),
                        VisibilityRule.builder().userType("client").triggerType(null).invokeMode("batch_upload")
                                .execNodes(List.of("process_end")).build(),
                        VisibilityRule.builder().userType("client").triggerType(null).invokeMode("batch_download")
                                .execNodes(List.of("process_end")).build()
                ))
                .defaults(Map.of("algorithm", "SHA256"))
                .requiredFields(List.of())
                .fieldLabels(Map.of())
                .build());

        sftpPluginDefinitionRepository.save(SftpPluginDefinition.builder()
                .name("virus_scan")
                .label("病毒扫描")
                .description("对上传的文件进行病毒扫描，保障系统安全")
                .visibility(List.of(
                        VisibilityRule.builder().userType("server").triggerType("file_upload").invokeMode(null)
                                .execNodes(List.of("after_upload")).build()
                ))
                .defaults(Map.of("engine", "clamav", "customEnginePath", "", "quarantineInfected", true, "scanTimeout", 60))
                .requiredFields(List.of("engine"))
                .fieldLabels(Map.of("engine", "扫描引擎", "customEnginePath", "自定义引擎路径"))
                .build());

        sftpPluginDefinitionRepository.save(SftpPluginDefinition.builder()
                .name("s3_upload")
                .label("S3 文件上传")
                .description("将文件上传到 Amazon S3 兼容的对象存储")
                .visibility(List.of(
                        VisibilityRule.builder().userType("server").triggerType("file_download").invokeMode(null)
                                .execNodes(List.of("after_download")).build(),
                        VisibilityRule.builder().userType("client").triggerType(null).invokeMode("sftp_download")
                                .execNodes(List.of("after_download")).build(),
                        VisibilityRule.builder().userType("client").triggerType(null).invokeMode("sftp_upload")
                                .execNodes(List.of("after_upload")).build(),
                        VisibilityRule.builder().userType("client").triggerType(null).invokeMode("batch_upload")
                                .execNodes(List.of("process_end")).build(),
                        VisibilityRule.builder().userType("client").triggerType(null).invokeMode("batch_download")
                                .execNodes(List.of("process_end")).build()
                ))
                .defaults(Map.of(
                        "endpoint", "", "bucket", "", "region", "", "accessKey", "", "secretKey", "",
                        "targetPrefix", "", "useHttps", true, "pathStyle", false
                ))
                .requiredFields(List.of("endpoint", "bucket", "accessKey", "secretKey"))
                .fieldLabels(Map.of(
                        "endpoint", "S3 Endpoint", "bucket", "存储桶名称",
                        "accessKey", "Access Key", "secretKey", "Secret Key"
                ))
                .build());

        sftpPluginDefinitionRepository.save(SftpPluginDefinition.builder()
                .name("api_call")
                .label("API 调用")
                .description("通过 HTTP 请求调用外部 API")
                .visibility(List.of(
                        VisibilityRule.builder().userType("server").triggerType("file_upload").invokeMode(null)
                                .execNodes(List.of("before_upload", "after_upload")).build(),
                        VisibilityRule.builder().userType("server").triggerType("file_download").invokeMode(null)
                                .execNodes(List.of("before_download", "after_download")).build(),
                        VisibilityRule.builder().userType("client").triggerType(null).invokeMode("sftp_download")
                                .execNodes(List.of("before_download", "after_download")).build(),
                        VisibilityRule.builder().userType("client").triggerType(null).invokeMode("sftp_upload")
                                .execNodes(List.of("before_upload", "after_upload")).build(),
                        VisibilityRule.builder().userType("client").triggerType(null).invokeMode("batch_upload")
                                .execNodes(List.of("before_upload", "after_upload", "process_start", "process_end")).build(),
                        VisibilityRule.builder().userType("client").triggerType(null).invokeMode("batch_download")
                                .execNodes(List.of("before_download", "after_download", "process_start", "process_end")).build()
                ))
                .defaults(Map.of("method", "POST", "url", "", "headers", "", "body", "", "connectTimeout", 5000, "readTimeout", 30000))
                .requiredFields(List.of("url"))
                .fieldLabels(Map.of("url", "请求URL"))
                .build());

        sftpPluginDefinitionRepository.save(SftpPluginDefinition.builder()
                .name("kafka")
                .label("Kafka 消息通知")
                .description("发送消息到 Kafka 消息队列")
                .visibility(List.of(
                        VisibilityRule.builder().userType("server").triggerType("file_upload").invokeMode(null)
                                .execNodes(List.of("before_upload", "after_upload")).build(),
                        VisibilityRule.builder().userType("server").triggerType("file_download").invokeMode(null)
                                .execNodes(List.of("before_download", "after_download")).build(),
                        VisibilityRule.builder().userType("client").triggerType(null).invokeMode("sftp_download")
                                .execNodes(List.of("before_download", "after_download")).build(),
                        VisibilityRule.builder().userType("client").triggerType(null).invokeMode("sftp_upload")
                                .execNodes(List.of("before_upload", "after_upload")).build(),
                        VisibilityRule.builder().userType("client").triggerType(null).invokeMode("batch_upload")
                                .execNodes(List.of("before_upload", "after_upload", "process_start", "process_end")).build(),
                        VisibilityRule.builder().userType("client").triggerType(null).invokeMode("batch_download")
                                .execNodes(List.of("before_download", "after_download", "process_start", "process_end")).build()
                ))
                .defaults(Map.of("bootstrapServers", "", "topic", "", "key", "", "messageTemplate", ""))
                .requiredFields(List.of("bootstrapServers", "topic"))
                .fieldLabels(Map.of("bootstrapServers", "Bootstrap Servers", "topic", "Topic"))
                .build());

        sftpPluginDefinitionRepository.save(SftpPluginDefinition.builder()
                .name("rocketmq")
                .label("RocketMQ 消息通知")
                .description("发送消息到 RocketMQ 消息队列")
                .visibility(List.of(
                        VisibilityRule.builder().userType("server").triggerType("file_upload").invokeMode(null)
                                .execNodes(List.of("before_upload", "after_upload")).build(),
                        VisibilityRule.builder().userType("server").triggerType("file_download").invokeMode(null)
                                .execNodes(List.of("before_download", "after_download")).build(),
                        VisibilityRule.builder().userType("client").triggerType(null).invokeMode("sftp_download")
                                .execNodes(List.of("before_download", "after_download")).build(),
                        VisibilityRule.builder().userType("client").triggerType(null).invokeMode("sftp_upload")
                                .execNodes(List.of("before_upload", "after_upload")).build(),
                        VisibilityRule.builder().userType("client").triggerType(null).invokeMode("batch_upload")
                                .execNodes(List.of("before_upload", "after_upload", "process_start", "process_end")).build(),
                        VisibilityRule.builder().userType("client").triggerType(null).invokeMode("batch_download")
                                .execNodes(List.of("before_download", "after_download", "process_start", "process_end")).build()
                ))
                .defaults(Map.of("nameServer", "", "topic", "", "producerGroup", "", "tag", "", "key", "", "messageTemplate", ""))
                .requiredFields(List.of("nameServer", "topic", "producerGroup"))
                .fieldLabels(Map.of("nameServer", "NameServer地址", "topic", "Topic", "producerGroup", "生产者组"))
                .build());
    }
}
