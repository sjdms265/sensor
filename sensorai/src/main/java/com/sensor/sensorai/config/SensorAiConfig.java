package com.sensor.sensorai.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestTemplate;

@Configuration
public class SensorAiConfig {

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
