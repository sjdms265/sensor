package com.sensor.sensorai.controller;

import com.sensor.sensorai.dto.Rain;
import com.sensor.sensorai.dto.SensorStatsResults;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/sensorai")
@RequiredArgsConstructor
@Slf4j
public class ChatController {

    private final ChatClient chatClient;

    @GetMapping("/stats/{userId}/{sensorId}")
    public ResponseEntity<SensorStatsResults> getBasicStats(HttpServletRequest request, @PathVariable final String userId,
                                                final @PathVariable String sensorId) {

        String contents = "Calculate average value, highest value and lowest value for the user {userId} and sensor {sensorId}. " +
                "Use a list size of {pageSize} SensorEndpoints and use {token}";
        log.info("getBasicStats request to ai: {}", contents);

        SensorStatsResults answer =  chatClient.prompt().
                user(contents).user(userSpec -> userSpec.text(contents).param("userId", userId).
                        param("sensorId", sensorId).param("pageSize", 50).param("token", request.getHeader("Authorization"))).
                call().entity(SensorStatsResults.class, ChatClient.EntityParamSpec::validateSchema);

        log.info("getBasicStats answer: {}", answer);

        return ResponseEntity.ok(answer);

    }

    @GetMapping("/rain/{userId}")
    public ResponseEntity<Rain> rainProbability(HttpServletRequest request, @PathVariable final String userId) {

        String contents = "What is the probability of rain today for the user {userId}?. " +
                "To get the last values of temperature and humidity use the get-sensor-info-by-userId-and-pattern tool " +
                "with userId={userId}, pattern={pattern} and token={token}.";

        log.info("rainProbability request to ai: {}", contents);

        Rain answer =  chatClient.prompt().user(userSpec -> userSpec.text(contents).param("userId", userId).
                param("pattern", "temperature,humidity").param("token", request.getHeader("Authorization"))).
                call().entity(Rain.class, ChatClient.EntityParamSpec::validateSchema);

        log.info("rainProbability answer: {}", answer);

        return ResponseEntity.ok(answer);
    }

    @GetMapping("/stats/hello")
    public ResponseEntity<SensorStatsResults> getChatModel(HttpServletRequest request) {
        return getBasicStats(request, "sjdms265", "sensor.10000db11e_t");
    }

}
