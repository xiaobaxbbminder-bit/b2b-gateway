package com.whatsoeversky.minder.agent.a2a;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.a2a.server.ServerCallContext;
import io.a2a.server.auth.UnauthenticatedUser;
import io.a2a.spec.*;
import io.a2a.transport.jsonrpc.context.JSONRPCContextKeys;
import io.a2a.transport.jsonrpc.handler.JSONRPCHandler;
import io.a2a.util.Utils;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Flow;
import java.util.concurrent.atomic.AtomicLong;

@RestController
@Slf4j
public class A2AController {

    @Resource
    private AgentCard agentCard;

    @Resource
    private JSONRPCHandler jsonRpcHandler;

    @Resource
    private ObjectMapper objectMapper;

    @GetMapping("/.well-known/agent-card.json")
    public AgentCard agentCard() {
        return agentCard;
    }


    @PostMapping("/agent/jsonrpc")
    public Object jsonrpc(@RequestBody String body) {
        try {
            log.info("body: {}", body);
            Map<String, Object> raw = objectMapper.readValue(body, Map.class);
            String method = (String) raw.get("method");
            Object id = raw.get("id");

            ServerCallContext callContext = createCallContext(method);
            return switch (method) {
                case SendMessageRequest.METHOD -> {
                    SendMessageRequest req = objectMapper.readValue(body, SendMessageRequest.class);
                    yield jsonRpcHandler.onMessageSend(req, callContext);
                }
                case GetTaskRequest.METHOD -> {
                    GetTaskRequest req = objectMapper.readValue(body, GetTaskRequest.class);
                    yield jsonRpcHandler.onGetTask(req, callContext);
                }
                case CancelTaskRequest.METHOD -> {
                    CancelTaskRequest req = objectMapper.readValue(body, CancelTaskRequest.class);
                    yield jsonRpcHandler.onCancelTask(req, callContext);
                }
                case SetTaskPushNotificationConfigRequest.METHOD -> {
                    SetTaskPushNotificationConfigRequest req = objectMapper.readValue(body, SetTaskPushNotificationConfigRequest.class);
                    yield jsonRpcHandler.setPushNotificationConfig(req, callContext);
                }
                case GetTaskPushNotificationConfigRequest.METHOD -> {
                    GetTaskPushNotificationConfigRequest req = objectMapper.readValue(body, GetTaskPushNotificationConfigRequest.class);
                    yield jsonRpcHandler.getPushNotificationConfig(req, callContext);
                }
                case ListTaskPushNotificationConfigRequest.METHOD -> {
                    ListTaskPushNotificationConfigRequest req = objectMapper.readValue(body, ListTaskPushNotificationConfigRequest.class);
                    yield jsonRpcHandler.listPushNotificationConfig(req, callContext);
                }
                case DeleteTaskPushNotificationConfigRequest.METHOD -> {
                    DeleteTaskPushNotificationConfigRequest req = objectMapper.readValue(body, DeleteTaskPushNotificationConfigRequest.class);
                    yield jsonRpcHandler.deletePushNotificationConfig(req, callContext);
                }
                case GetAuthenticatedExtendedCardRequest.METHOD -> {
                    GetAuthenticatedExtendedCardRequest req = objectMapper.readValue(body, GetAuthenticatedExtendedCardRequest.class);
                    yield jsonRpcHandler.onGetAuthenticatedExtendedCardRequest(req, callContext);
                }
                case SendStreamingMessageRequest.METHOD -> handleStreaming(body, callContext, true);
                case TaskResubscriptionRequest.METHOD -> handleStreaming(body, callContext, false);
                default ->
                        new JSONRPCErrorResponse(id, new UnsupportedOperationError(-32601, "Method not found: " + method, null));
            };
        } catch (JSONRPCError e) {
            return new JSONRPCErrorResponse(e);
        } catch (Exception e) {
            log.error("JSON-RPC processing error", e);
            return new JSONRPCErrorResponse(new io.a2a.spec.InternalError(e.getMessage()));
        }
    }

    private SseEmitter handleStreaming(String body, ServerCallContext callContext, boolean isSendSubscribe) {
        SseEmitter emitter = new SseEmitter(0L);
        AtomicLong eventId = new AtomicLong(0);


        try {
            Flow.Publisher<SendStreamingMessageResponse> publisher;
            if (isSendSubscribe) {
                SendStreamingMessageRequest req = Utils.unmarshalFrom(body, new TypeReference<SendStreamingMessageRequest>() {
                });
                // SendStreamingMessageRequest req = objectMapper.readValue(body, SendStreamingMessageRequest.class);
                publisher = jsonRpcHandler.onMessageSendStream(req, callContext);
            } else {
                TaskResubscriptionRequest req = Utils.unmarshalFrom(body, new TypeReference<TaskResubscriptionRequest>() {
                });
                // TaskResubscriptionRequest req = objectMapper.readValue(body, TaskResubscriptionRequest.class);
                publisher = jsonRpcHandler.onResubscribeToTask(req, callContext);
            }

            publisher.subscribe(new Flow.Subscriber<>() {
                @Override
                public void onSubscribe(Flow.Subscription subscription) {
                    subscription.request(Long.MAX_VALUE);
                    emitter.onTimeout(subscription::cancel);
                    emitter.onCompletion(subscription::cancel);
                    emitter.onError(e -> subscription.cancel());
                }

                @Override
                public void onNext(SendStreamingMessageResponse response) {
                    try {
                        String json = Utils.toJsonString(response);
                        // String json = objectMapper.writeValueAsString(response);
                        emitter.send(SseEmitter.event()
                                .id(String.valueOf(eventId.incrementAndGet()))
                                .data(json, MediaType.APPLICATION_JSON));
                    } catch (Exception e) {
                        log.error("SSE send error", e);
                        emitter.completeWithError(e);
                    }
                }

                @Override
                public void onError(Throwable throwable) {
                    log.error("Streaming error", throwable);
                    try {
                        JSONRPCError error = throwable instanceof JSONRPCError jsonrpcError ? jsonrpcError
                                : new io.a2a.spec.InternalError(throwable.getMessage());
                        String json = objectMapper.writeValueAsString(new JSONRPCErrorResponse(error));
                        emitter.send(SseEmitter.event()
                                .id(String.valueOf(eventId.incrementAndGet()))
                                .data(json, MediaType.APPLICATION_JSON));
                    } catch (Exception e) {
                        log.error("SSE error send failed", e);
                    } finally {
                        emitter.completeWithError(throwable);
                    }
                }

                @Override
                public void onComplete() {
                    emitter.complete();
                }
            });
        } catch (Exception e) {
            log.error("Streaming setup error", e);
            emitter.completeWithError(e);
        }

        return emitter;
    }

    private ServerCallContext createCallContext(String method) {
        Map<String, Object> state = Collections.singletonMap(JSONRPCContextKeys.METHOD_NAME_KEY, method);
        return new ServerCallContext(UnauthenticatedUser.INSTANCE, state, Set.of());
    }
}
