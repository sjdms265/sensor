package com.sensor.sensormcpserver.config;

import com.sensor.sensormcpserver.service.SensorTools;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

//https://dzone.com/articles/spring-ai-mcp-server-client-tutorial
@Configuration
public class McpConfig {

    @Bean
    public ToolCallbackProvider toolCallbackProvider(SensorTools sensorTools) {
        return MethodToolCallbackProvider.builder()
                .toolObjects(sensorTools)
                .build();
    }
}
