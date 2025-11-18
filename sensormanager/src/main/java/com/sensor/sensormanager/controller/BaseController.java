package com.sensor.sensormanager.controller;

import com.sensor.sensormanager.dto.SensorEndpointDTO;
import com.sensor.sensormanager.util.SensorManagerUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
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

    @GetMapping()
    public Map<String, String> home(HttpServletRequest request, HttpServletResponse response, @AuthenticationPrincipal User user) throws IOException {

        Map<String, String> model = sensorManagerUtil.writeTokensResponse(request, response, user.getAuthorities().
                stream().map(GrantedAuthority::getAuthority).toList(), user.getUsername());

        model.put("user", String.format("Hello %s to Sensor Manager", user.getUsername()));
        return model;
    }

    @PostMapping(BASE_PATH + "/echoSensorEndpoint")
    public SensorEndpointDTO echoEndpoint(@RequestBody SensorEndpointDTO sensorEndpointDTO) {

        log.info("echoEndpoint {}", sensorEndpointDTO);

        streamBridge.send("sensorEcho", sensorEndpointDTO);

        return sensorEndpointDTO;
    }

    @GetMapping(BASE_PATH + "/echoSensorEndpoint")
    public String echoEndpoint() {
        log.info("hello echoEndpoint ");
        return "hello echoEndpoint";
    }
}
