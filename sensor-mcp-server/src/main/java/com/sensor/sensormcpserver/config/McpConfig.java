package com.sensor.sensormcpserver.config;

import com.sensor.sensormcpserver.service.SensorTools;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

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
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}
