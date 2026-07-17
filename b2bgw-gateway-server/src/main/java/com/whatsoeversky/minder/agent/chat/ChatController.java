package com.whatsoeversky.minder.agent.chat;

import com.whatsoeversky.minder.common.Result;
import com.whatsoeversky.minder.agent.dto.ChatReqDto;
import com.whatsoeversky.minder.agent.tools.SearchDocsTools;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.http.MediaType;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import reactor.core.publisher.Flux;

import java.io.IOException;

@RestController
@RequestMapping("/api/ai")
@Slf4j
public class ChatController {

    @Resource
    private ChatClient.Builder builder;

    @Resource
    private ChatModel chatModel;

    @Resource
    private MessageChatMemoryAdvisor messageChatMemoryAdvisor;

    @Resource
    private SearchDocsTools searchDocsTools;

    private ChatClient buildClient() {
        return ChatClient.builder(chatModel)
                .defaultAdvisors(messageChatMemoryAdvisor)
                .defaultTools(searchDocsTools)
                .build();
    }

    @PostMapping(value = "/chat", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter chat(@RequestBody ChatReqDto req) {
        SseEmitter emitter = new SseEmitter(0L);
        ChatClient chatClient = buildClient();

        Flux<String> stream = chatClient.prompt()
                .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, req.getConversationId()))
                .system("你是 SFTP RPA 系统的 AI 助手。"
                        + "当用户询问系统的使用方法、配置步骤、业务场景等知识性问题时，"
                        + "请使用 searchDocs 工具搜索培训文档获取准确信息。"
                        + "如果引用了文档内容，在回答末尾标注来源章节。请用中文回答。")
                .user(req.getMessage())
                .stream()
                .content();

        stream.subscribe(
                content -> {
                    if (!StringUtils.hasLength(content)) return;
                    try {
                        emitter.send(SseEmitter.event().data(content));
                    } catch (IOException e) {
                        emitter.completeWithError(e);
                    }
                },
                error -> {
                    log.error("AI stream error", error);
                    emitter.completeWithError(error);
                },
                () -> emitter.complete()
        );
        return emitter;
    }

    @PostMapping("/chat/sync")
    public Result<String> chatSync(@RequestBody ChatReqDto req) {
        ChatClient chatClient = buildClient();
        String content = chatClient.prompt()
                .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, req.getConversationId()))
                .system("你是 SFTP RPA 系统的 AI 助手。"
                        + "当用户询问系统的使用方法、配置步骤、业务场景等知识性问题时，"
                        + "请使用 searchDocs 工具搜索培训文档获取准确信息。"
                        + "如果引用了文档内容，在回答末尾标注来源章节。请用中文回答。")
                .user(req.getMessage())
                .call()
                .content();
        return Result.success(content);
    }
}
