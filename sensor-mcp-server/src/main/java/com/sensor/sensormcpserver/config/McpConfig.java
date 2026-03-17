package com.sensor.sensormcpserver.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.sensor.sensormcpserver.service.SensorTools;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

//https://dzone.com/articles/spring-ai-mcp-server-client-tutorial
@Configuration
public class McpConfig {

    @Bean
    public ToolCallbackProvider toolCallbackProvider(SensorTools sensorTools) {
        return MethodToolCallbackProvider.builder()
                .toolObjects(sensorTools)
                .build();
    }

    @Bean
    public WebClient webClient() {
        return WebClient.builder().build();
    }

    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper().registerModule(new JavaTimeModule());
    }
}
