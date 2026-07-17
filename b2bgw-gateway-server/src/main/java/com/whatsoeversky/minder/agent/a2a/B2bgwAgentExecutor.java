package com.whatsoeversky.minder.agent.a2a;


import io.a2a.server.agentexecution.AgentExecutor;
import io.a2a.server.agentexecution.RequestContext;
import io.a2a.server.events.EventQueue;
import io.a2a.server.tasks.TaskUpdater;
import io.a2a.spec.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class B2bgwAgentExecutor implements AgentExecutor {
    @Override
    public void execute(RequestContext context, EventQueue eventQueue) throws JSONRPCError {
        String userMessage = extractTextFromMessage(context.getMessage());
        String taskId = context.getTaskId();
        String contextId = context.getContextId();

        log.info("user message: {}", userMessage);

        // 获取到用户消息，
        // 意图识别（协议是SFTP、OpenAI、RN、AS2）
        // 如果是SFTP，再次进行意图识别：配置服务端账号还是客户端账号
        // 如果是服务端账号，需要用户提供自定义用户名和密码
        // 追问是否需要配置插件和异常告警
        // 以上追问的环节需要LLM生成A2UI的数据

        TaskUpdater taskUpdater = new TaskUpdater(context, eventQueue);

        eventQueue.enqueueEvent(new Task.Builder()
                .id(taskId)
                .contextId(contextId)
                .history(context.getMessage())
                .status(new TaskStatus(TaskState.SUBMITTED))
                .build());

        eventQueue.enqueueEvent(new TaskStatusUpdateEvent.Builder()
                .taskId(taskId)
                .contextId(contextId)
                .status(new TaskStatus(TaskState.WORKING))
                .build());

        StringBuilder responseBuilder = new StringBuilder();

//        streamingChatModel.chat(userMessage, new StreamingChatResponseHandler() {
//            @Override
//            public void onPartialResponse(String partialResponse) {
//                responseBuilder.append(partialResponse);
//                eventQueue.enqueueEvent(new TaskArtifactUpdateEvent.Builder()
//                        .taskId(taskId)
//                        .contextId(contextId)
//                        .artifact(new Artifact.Builder()
//                                .artifactId("response")
//                                .parts(new TextPart(responseBuilder.toString()))
//                                .build())
//                        .build());
//            }
//
//            @Override
//            public void onCompleteResponse(ChatResponse response) {
//                log.info("chat response completed, total length: {}", responseBuilder.length());
//                eventQueue.enqueueEvent(new TaskStatusUpdateEvent.Builder()
//                        .taskId(taskId)
//                        .contextId(contextId)
//                        .status(new TaskStatus(TaskState.COMPLETED))
//                        .isFinal(true)
//                        .build());
//                eventQueue.close();
//            }
//
//            @Override
//            public void onError(Throwable error) {
//                log.error("chat streaming error", error);
//            }
//        });
    }

    @Override
    public void cancel(RequestContext context, EventQueue eventQueue) throws JSONRPCError {
        throw new UnsupportedOperationError();
    }

    private String extractTextFromMessage(Message message) {
        if (message == null || message.getParts() == null) {
            return "";
        }
        StringBuilder textBuilder = new StringBuilder();
        for (Part<?> part : message.getParts()) {
            if (part instanceof TextPart textPart) {
                textBuilder.append(textPart.getText());
            }
        }
        return textBuilder.toString();
    }
}
