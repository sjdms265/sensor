package com.sensor.sensorai.config;

import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.ai.ollama.api.OllamaApi;
import org.springframework.ai.ollama.api.OllamaOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestTemplate;

@Configuration
public class SensorAiConfig {

    @Value("${spring.ai.ollama.chat.options.model}")
    private String model;

    @Value("${spring.ai.ollama.chat.options.temperature}")
    private Double temperature;

    @Value("${spring.ai.ollama.base-url}")
    private String baseUrl;

    @Bean
    OllamaChatModel ollamaChatModel() {

        return new OllamaChatModel(new OllamaApi(baseUrl),
                OllamaOptions.create().withModel(model).withTemperature(temperature));
    }

    @Bean
    //https://www.geeksforgeeks.org/spring-webclient-load-balancing/
    public RestClient.Builder restClientBuilder() {
        return RestClient.builder();
    }

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}
