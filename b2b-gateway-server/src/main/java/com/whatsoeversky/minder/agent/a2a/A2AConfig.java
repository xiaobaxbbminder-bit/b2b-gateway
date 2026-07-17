package com.whatsoeversky.minder.agent.a2a;

import io.a2a.server.agentexecution.AgentExecutor;
import io.a2a.server.config.A2AConfigProvider;
import io.a2a.server.config.DefaultValuesConfigProvider;
import io.a2a.server.events.InMemoryQueueManager;
import io.a2a.server.requesthandlers.DefaultRequestHandler;
import io.a2a.server.requesthandlers.RequestHandler;
import io.a2a.server.tasks.BasePushNotificationSender;
import io.a2a.server.tasks.InMemoryPushNotificationConfigStore;
import io.a2a.server.tasks.InMemoryTaskStore;
import io.a2a.server.tasks.PushNotificationSender;
import io.a2a.spec.AgentCapabilities;
import io.a2a.spec.AgentCard;
import io.a2a.spec.AgentSkill;
import io.a2a.transport.jsonrpc.handler.JSONRPCHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

@Configuration
public class A2AConfig {

    private static final String AGENT_URL = "http://localhost:9030/agent/jsonrpc";

    @Bean
    public AgentCard agentCard() {
        return new AgentCard.Builder()
                .name("B2B网关Agent")
                .description("配置B2B网关相关协议，包括SFTP协议、RosettaNet协议、AS2协议、OpenAPI协议")
                .url(AGENT_URL)
                .version("1.0.0")
                .capabilities(new AgentCapabilities.Builder()
                        .streaming(true)
                        .pushNotifications(false)
                        .build())
                .defaultInputModes(Collections.singletonList("text"))
                .defaultOutputModes(Collections.singletonList("text"))
                .skills(Collections.singletonList(new AgentSkill.Builder()
                        .id("b2bgw_skill")
                        .name("b2bgw skill")
                        .description("帮助解决B2B通信问题")
                        .tags(Collections.singletonList("b2bgw"))
                        .examples(List.of("请帮我配置一个SFTP服务器，账号为user1，密码为pass1，伙伴上传文件到该SFTP服务器上"))
                        .build()))
                .build();
    }

    @Bean
    public Executor a2aExecutor() {
        return Executors.newCachedThreadPool();
    }

    @Bean
    public InMemoryTaskStore taskStore() {
        return new InMemoryTaskStore();
    }

    @Bean
    public InMemoryPushNotificationConfigStore pushNotificationConfigStore() {
        return new InMemoryPushNotificationConfigStore();
    }


    @Bean
    public InMemoryQueueManager queueManager(InMemoryTaskStore taskStore) {
        return new InMemoryQueueManager(taskStore);
    }


    @Bean
    public PushNotificationSender pushNotificationSender(InMemoryPushNotificationConfigStore configStore) {
        return new BasePushNotificationSender(configStore);
    }

    @Bean
    public RequestHandler requestHandler(
            AgentExecutor agentExecutor,
            InMemoryTaskStore taskStore,
            InMemoryQueueManager queueManager,
            InMemoryPushNotificationConfigStore pushNotificationConfigStore,
            PushNotificationSender pushNotificationSender,
            Executor a2aExecutor) {

        return DefaultRequestHandler.create(
                agentExecutor,
                taskStore,
                queueManager,
                pushNotificationConfigStore,
                pushNotificationSender,
                a2aExecutor
        );
    }

    @Bean
    public A2AConfigProvider a2AConfigProvider() {
        return new DefaultValuesConfigProvider();
    }

    @Bean
    public JSONRPCHandler jsonRpcHandler(
            AgentCard agentCard,
            RequestHandler requestHandler,
            Executor a2aExecutor) {
        return new JSONRPCHandler(agentCard, requestHandler, a2aExecutor);
    }
}
