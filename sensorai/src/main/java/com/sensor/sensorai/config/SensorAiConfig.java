package com.sensor.sensorai.config;

import org.springframework.ai.anthropic.AnthropicChatModel;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class SensorAiConfig {

    /*@Bean
    //https://www.geeksforgeeks.org/spring-webclient-load-balancing/
    public RestClient.Builder restClientBuilder() {
        return RestClient.builder();
    }*/


    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    @Bean(name = "chatClient")
    @ConditionalOnProperty(name = "spring.ai.active-model", havingValue = "ollama")
    public ChatClient ollamaChatClient(OllamaChatModel ollamaChatModel) {

        var defaultSystem = """
                You are an Backend AI powered assistant that analyze JSON information that contains IOT sensor data with temperature, humidity.
                """;

        return ChatClient.builder(ollamaChatModel).defaultSystem(defaultSystem).build();
    }

    @Bean(name = "chatClient")
    @ConditionalOnProperty(name = "spring.ai.active-model", havingValue = "anthropic")
    public ChatClient anthropicChatClient(AnthropicChatModel anthropicChatModel) {

        var defaultSystem = """
                You are an Backend AI powered assistant that analyze JSON information that contains IOT sensor data with temperature, humidity.
                """;

        return ChatClient.builder(anthropicChatModel).defaultSystem(defaultSystem).build();
    }

}
