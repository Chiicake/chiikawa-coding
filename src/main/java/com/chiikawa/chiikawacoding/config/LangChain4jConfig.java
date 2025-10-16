package com.chiikawa.chiikawacoding.config;

import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.model.openai.OpenAiStreamingChatModel;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.StreamingChatModel;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class LangChain4jConfig {

    @Value("${langchain4j.open-ai.chat-model.base-url}")
    private String baseUrl;

    @Value("${langchain4j.open-ai.chat-model.api-key}")
    private String apiKey;

    @Value("${langchain4j.open-ai.chat-model.model-name}")
    private String modelName;

    @Value("${langchain4j.open-ai.chat-model.log-requests}")
    private boolean logRequests;

    @Value("${langchain4j.open-ai.chat-model.log-responses}")
    private boolean logResponses;

    @Value("${langchain4j.open-ai.chat-model.max-tokens}")
    private Integer maxTokens;

    @Value("${langchain4j.open-ai.chat-model.strict-json-schema}")
    private boolean strictJsonSchema;

    @Value("${langchain4j.open-ai.chat-model.response-format}")
    private String responseFormat;

    @Bean
    public ChatModel chatModel() {
        return OpenAiChatModel.builder()
                .baseUrl(baseUrl)
                .apiKey(apiKey)
                .modelName(modelName)
                .logRequests(logRequests)
                .logResponses(logResponses)
                .maxTokens(maxTokens)
                .strictJsonSchema(strictJsonSchema)
                .responseFormat(responseFormat)
                .build();
    }

    @Bean
    public StreamingChatModel streamingChatModel() {
        return OpenAiStreamingChatModel.builder()
                .baseUrl(baseUrl)
                .apiKey(apiKey)
                .modelName(modelName)
                .logRequests(logRequests)
                .logResponses(logResponses)
                .maxTokens(maxTokens)
                .strictJsonSchema(strictJsonSchema)
                .responseFormat(responseFormat)
                .build();
    }
}
