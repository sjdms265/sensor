package com.sensor.sensorai.config;

import org.springframework.ai.anthropic.AnthropicChatModel;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.deepseek.DeepSeekChatModel;
import org.springframework.ai.mcp.AsyncMcpToolCallbackProvider;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.ai.ollama.api.OllamaChatOptions;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class SensorAiConfig {

    private static final String DEFAULT_SYSTEM = """
                You are an Backend AI powered assistant that analyze JSON information that contains IOT sensor data with temperature, humidity.
                """;

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    @Bean(name = "chatClient")
    @ConditionalOnProperty(name = "spring.ai.active-model", havingValue = "ollama")
    public ChatClient ollamaChatClient(OllamaChatModel ollamaChatModel, AsyncMcpToolCallbackProvider toolCallbackProvider) {

        return ChatClient.builder(ollamaChatModel).defaultSystem(DEFAULT_SYSTEM).defaultTools(toolCallbackProvider).
                defaultOptions(OllamaChatOptions.builder()
                .format("json") // Force Ollama's underlying engine into rigid JSON mode
                .temperature(0.0) // Keep output deterministic to prevent rambling
                ).build();
    }

    @Bean(name = "chatClient")
    @ConditionalOnProperty(name = "spring.ai.active-model", havingValue = "anthropic")
    public ChatClient anthropicChatClient(AnthropicChatModel anthropicChatModel, AsyncMcpToolCallbackProvider toolCallbackProvider) {

        return ChatClient.builder(anthropicChatModel).defaultSystem(DEFAULT_SYSTEM).defaultTools(toolCallbackProvider).build();
    }

    
    @Bean(name = "deepseek")
    @ConditionalOnProperty(name = "spring.ai.active-model", havingValue = "deepseek")
    public ChatClient deepseekChatClient(DeepSeekChatModel deepSeekChatModel, AsyncMcpToolCallbackProvider toolCallbackProvider) {

        return ChatClient.builder(deepSeekChatModel).defaultSystem(DEFAULT_SYSTEM).defaultTools(toolCallbackProvider).build();
    }

}
