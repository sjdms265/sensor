package com.sensor.sensormanager.controller;

import com.sensor.sensorcommon.dto.SensorEndpointDTO;
import com.sensor.sensormanager.config.SensorValueWebSocketHandler;
import com.sensor.sensormanager.util.SensorManagerUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.User;
import org.springframework.util.StreamUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Map;

@RestController
@RequestMapping("/")
@RequiredArgsConstructor
@Slf4j
public class BaseController {

    public static final String BASE_PATH = "/api";
    public static final String ADMIN_PATH = "/admin";
    public static final String AUTH_PATH = BASE_PATH + "/auth";

    private final SensorManagerUtil sensorManagerUtil;

    private final StreamBridge streamBridge;

    private final SensorValueWebSocketHandler sensorValueWebSocketHandler;

    @Value(value = "${sensor-manager.topic.sensor-value}")
    private String topic;

    @GetMapping()
    public Map<String, String> home(HttpServletRequest request, HttpServletResponse response, @AuthenticationPrincipal User user) throws IOException {

        Map<String, String> model = sensorManagerUtil.writeTokensResponse(request, response, user.getAuthorities().
                stream().map(GrantedAuthority::getAuthority).toList(), user.getUsername());

        model.put("user", String.format("Hello %s to Sensor Manager", user.getUsername()));
        return model;
    }

    @PostMapping(BASE_PATH + "/echoSensorEndpoint")
    public SensorEndpointDTO echoEndpoint(@RequestBody SensorEndpointDTO sensorEndpointDTO) throws Exception {

        log.debug("echoEndpoint {}", sensorEndpointDTO);

        streamBridge.send(topic, MessageBuilder.withPayload(sensorEndpointDTO)
                .setHeader(KafkaHeaders.KEY, sensorEndpointDTO.getUserId() + "-" + sensorEndpointDTO.getSensorId())
                .build());

        sensorValueWebSocketHandler.sendMessage(sensorEndpointDTO);

        return sensorEndpointDTO;
    }

    @GetMapping(BASE_PATH + "/echoSensorEndpoint")
    public String echoEndpoint(HttpServletRequest request) throws IOException {
        log.info("hello echoEndpoint {}", StreamUtils.copyToString(request.getInputStream(), StandardCharsets.UTF_8));
        return String.format("hello echoEndpoint %s", new Date());
    }
}
