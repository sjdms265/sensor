package com.sensor.sensormanager.controller;

import com.sensor.sensormanager.util.SensorManagerUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/")
@RequiredArgsConstructor
public class BaseController {

    public final static String BASE_PATH = "/api";

    private final SensorManagerUtil sensorManagerUtil;

    @GetMapping()
    public Map<String, String> home(HttpServletRequest request, HttpServletResponse response, @AuthenticationPrincipal User user) throws IOException {

        Map<String, String> model = sensorManagerUtil.writeTokensResponse(request, response, user.getAuthorities().
                stream().map(GrantedAuthority::getAuthority).collect(Collectors.toList()), user.getUsername());

        model.put("user", String.format("Hello %s to Sensor Manager", user.getUsername()));
        return model;
    }
}
