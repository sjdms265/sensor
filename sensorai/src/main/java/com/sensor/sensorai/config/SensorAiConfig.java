package com.sensor.sensorai.config;

import com.sensor.sensorai.advisor.AnthropicCacheUsageAdvisor;
import io.modelcontextprotocol.client.McpClient;
import io.modelcontextprotocol.client.McpSyncClient;
import io.modelcontextprotocol.client.transport.HttpClientSseClientTransport;
import io.modelcontextprotocol.spec.McpSchema;
import org.springframework.ai.anthropic.AnthropicChatModel;
import org.springframework.ai.anthropic.AnthropicChatOptions;
import org.springframework.ai.anthropic.api.AnthropicCacheOptions;
import org.springframework.ai.anthropic.api.AnthropicCacheStrategy;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.mcp.SyncMcpToolCallbackProvider;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;

@Configuration
public class SensorAiConfig {

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    @Bean
    public AnthropicCacheUsageAdvisor anthropicCacheUsageAdvisor() {
        return new AnthropicCacheUsageAdvisor();
    }

    @Bean(name = "chatClient")
    @ConditionalOnProperty(name = "spring.ai.active-model", havingValue = "ollama")
    public ChatClient ollamaChatClient(OllamaChatModel ollamaChatModel, McpSyncClient mcpSyncClient) {

        var defaultSystem = """
                You are an Backend AI powered assistant that analyze JSON information that contains IOT sensor data with temperature, humidity.
                """;

        return ChatClient.builder(ollamaChatModel).defaultSystem(defaultSystem).defaultToolCallbacks(new SyncMcpToolCallbackProvider(mcpSyncClient)).build();
    }

    @Bean(name = "chatClient")
    @ConditionalOnProperty(name = "spring.ai.active-model", havingValue = "anthropic")
    public ChatClient anthropicChatClient(AnthropicChatModel anthropicChatModel, McpSyncClient mcpSyncClient,
                                          AnthropicCacheUsageAdvisor cacheUsageAdvisor) {

        var defaultSystem = """
                You are an Backend AI powered assistant that analyze JSON information that contains IOT sensor data with temperature, humidity.
                """;

        //https://www.youtube.com/watch?v=eYb7BKW4QcU
        AnthropicCacheOptions cacheOptions = AnthropicCacheOptions.builder().strategy(AnthropicCacheStrategy.CONVERSATION_HISTORY).build();
        AnthropicChatOptions chatOptions = AnthropicChatOptions.builder().cacheOptions(cacheOptions).build();

        // Wrap the model so every individual API call (including mid-tool-loop calls) logs native usage
        // before Spring AI's tool-calling aggregation can drop the AnthropicApi.Usage native object.
        ChatModel observedModel = prompt -> {
            var response = anthropicChatModel.call(prompt);
            cacheUsageAdvisor.logUsage(response);
            return response;
        };

        return ChatClient.builder(observedModel).defaultSystem(defaultSystem).defaultOptions(chatOptions)
                .defaultToolCallbacks(new SyncMcpToolCallbackProvider(mcpSyncClient)).build();
    }

    @Bean
    public McpSyncClient mcpSyncClient(@Value("${mcp.sensor-mcp-server.base-url}") String baseUrl) {
        var transport = HttpClientSseClientTransport.builder(baseUrl)
                .sseEndpoint("mcp/sensor/sse")
                .build();

        McpSyncClient client = McpClient.sync(transport)
                .requestTimeout(Duration.ofSeconds(10))
                .clientInfo(new McpSchema.Implementation("MCP Sensor Client", "1.0.0"))
                .build();

        client.initialize();
        return client;
    }

}