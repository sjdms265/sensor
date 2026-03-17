package com.sensor.sensorai.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sensor.sensorcommon.dto.LoginSensorUserDTO;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.reactive.function.client.WebClient;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * End-to-end test for ChatController.
 * Requires the full stack to be running (sensormanager on :8090, sensorai on :8083).
 *
 * Run with: mvn test -pl sensorai -Dgroups=e2e
 */
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = ChatControllerE2ETest.TestConfig.class)
@TestPropertySource("classpath:application-e2e.properties")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Tag("e2e")
class ChatControllerE2ETest {

    @Configuration
    static class TestConfig {
        @Bean
        WebClient webClient() { return WebClient.builder().build(); }

        @Bean
        ObjectMapper objectMapper() { return new ObjectMapper(); }
    }

    @Value("${e2e.auth.url}")
    private String authUrl;

    @Value("${e2e.sensorai.url}")
    private String sensoraiUrl;

    @Value("${e2e.user.id}")
    private String userId;

    @Value("${e2e.auth.username}")
    private String username;

    @Value("${e2e.auth.password}")
    private String password;

    @Autowired
    private WebClient webClient;

    @Autowired
    private ObjectMapper objectMapper;

    private String bearerToken;

    @BeforeAll
    void obtainToken() throws Exception {
        LoginSensorUserDTO credentials = new LoginSensorUserDTO(username, password);

        String tokenJson = webClient.post()
                .uri(authUrl)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(credentials)
                .retrieve()
                .bodyToMono(String.class)
                .block();

        JsonNode tokenBody = objectMapper.readTree(tokenJson);
        bearerToken = "Bearer " + tokenBody.get("access_token").asText();
    }

    @Test
    void rainProbability_shouldReturn200_withProbabilityInResponse() throws Exception {
        String responseBody = webClient.get()
                .uri(sensoraiUrl + "/sensorai/rain/" + userId)
                .header("Authorization", bearerToken)
                .retrieve()
                .bodyToMono(String.class)
                .block();

        assertThat(responseBody).isNotBlank();

        JsonNode body = objectMapper.readTree(responseBody);
        assertThat(body.has("probability"))
                .as("Response should contain 'probability', got: %s", responseBody)
                .isTrue();
        assertThat(body.get("probability").isNumber())
                .as("'probability' should be numeric, got: %s", body.get("probability"))
                .isTrue();
    }

    @Test
    void rainProbability_shouldReturnProbabilityBetween0And100() throws Exception {
        String responseBody = webClient.get()
                .uri(sensoraiUrl + "/sensorai/rain/" + userId)
                .header("Authorization", bearerToken)
                .retrieve()
                .bodyToMono(String.class)
                .block();

        int probability = objectMapper.readTree(responseBody).get("probability").asInt();
        assertThat(probability).isBetween(0, 100);
    }
}