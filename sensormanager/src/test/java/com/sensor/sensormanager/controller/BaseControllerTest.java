package com.sensor.sensormanager.controller;

import com.sensor.sensormanager.config.SensorValueWebSocketHandler;
import com.sensor.sensormanager.util.SensorManagerUtil;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class BaseControllerTest {

    @Mock
    private SensorManagerUtil sensorManagerUtil;

    private StreamBridge streamBridge;

    private BaseController baseController;

    private SensorValueWebSocketHandler sensorValueWebSocketHandler;

    @BeforeEach
    void setUp() throws Exception {
        try (final AutoCloseable autoCloseable = MockitoAnnotations.openMocks(this)) {
            baseController = new BaseController(sensorManagerUtil, streamBridge, sensorValueWebSocketHandler);
        }
    }

    @Test
    void home() throws IOException {

        SimpleGrantedAuthority simpleGrantedAuthority = new SimpleGrantedAuthority("ROLE_USER");
        List<SimpleGrantedAuthority> simpleGrantedAuthorities = new ArrayList<>();
        simpleGrantedAuthorities.add(simpleGrantedAuthority);
        User user = new User("username", "password", simpleGrantedAuthorities);

        Map<String, String> tokens = new HashMap<>();
        tokens.put("access_token", "accessToken");
        tokens.put("refresh_token", "refreshToken");

        Mockito.when(sensorManagerUtil.writeTokensResponse(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any())).thenReturn(tokens);

        Map<String, String> responseMap = baseController.home(new MockHttpServletRequest(), new MockHttpServletResponse(), user);

        Assertions.assertNotNull(responseMap);
        Assertions.assertFalse(responseMap.isEmpty());
        Assertions.assertEquals(3, responseMap.size());
    }
}
