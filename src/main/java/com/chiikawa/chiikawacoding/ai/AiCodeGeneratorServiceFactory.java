package com.chiikawa.chiikawacoding.ai;

import dev.langchain4j.community.store.memory.chat.redis.RedisChatMemoryStore;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.service.AiServices;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AiCodeGeneratorServiceFactory {

    @Resource
    private ChatModel chatModel;

    @Resource
    private StreamingChatModel streamingChatModel;

    @Autowired
    private RedisChatMemoryStore redisChatMemoryStore;

    @Bean
    public AiCodeGeneratorService aiCodeGeneratorService() {
        return AiServices.builder(AiCodeGeneratorService.class)
                .chatModel(chatModel)
                .streamingChatModel(streamingChatModel)
                // 根据 id 构建独立的对话记忆
                .chatMemoryProvider(memoryId -> MessageWindowChatMemory
                        .builder()
                        .id(memoryId)
                        .chatMemoryStore(redisChatMemoryStore)
                        .maxMessages(20)
                        .build())
                .build();
    }

}
