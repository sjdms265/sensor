package com.sensor.sensorai.controller;

import com.sensor.sensorai.service.GraphqlSensorEndpointService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;

//https://spring.io/blog/2025/05/20/your-first-spring-ai-1
@Controller
@ResponseBody
@Slf4j
class SensorController {

    private final GraphqlSensorEndpointService graphqlSensorEndpointService;

    private final ChatClient anthropicChatClient;

    SensorController(GraphqlSensorEndpointService graphqlSensorEndpointService, ChatClient anthropicChatClient) {

        var defaultSystem = """
                You are an Backend AI powered assistant that analyze JSON information that contains IOT sensor data with temperature, humidity.
                """;

        this.anthropicChatClient = anthropicChatClient;

        this.graphqlSensorEndpointService = graphqlSensorEndpointService;
    }

    @GetMapping("/ai/{userId}/{sensorId}/{pageSize}")
    String getBasicStats(HttpServletRequest request,  @PathVariable String userId, @PathVariable String sensorId, @PathVariable Integer pageSize) {

        String sensorEndpoints = graphqlSensorEndpointService.getSensorEndpoints(request, userId, sensorId, pageSize);

        String question = "Analyze this json data and calculate the average temperature, highest temperature and lowest temperature: "
                + sensorEndpoints + "\n" + ChatController.responseFormat();
        log.info("request to ai: {}", question);

        return anthropicChatClient
                .prompt()
                .user(question)
                .call()
                .content();
    }
}
