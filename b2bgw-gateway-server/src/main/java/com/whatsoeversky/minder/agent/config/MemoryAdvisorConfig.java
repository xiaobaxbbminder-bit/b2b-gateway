package com.whatsoeversky.minder.agent.config;

import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemoryRepository;
import org.springframework.ai.chat.memory.InMemoryChatMemoryRepository;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MemoryAdvisorConfig {
    @Bean
    public MessageChatMemoryAdvisor messageChatMemoryAdvisor() {
        ChatMemoryRepository repository = new InMemoryChatMemoryRepository();
        MessageWindowChatMemory messageWindow = MessageWindowChatMemory.builder().chatMemoryRepository(repository).maxMessages(10).build();
        return MessageChatMemoryAdvisor.builder(messageWindow).build();
    }
}
