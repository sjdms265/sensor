package com.sensor.sensormanager.controller;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.sensor.sensorcommon.dto.LoginSensorUserDTO;
import com.sensor.sensormanager.model.SensorUser;
import com.sensor.sensormanager.service.UserService;
import com.sensor.sensormanager.util.SensorManagerUtil;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

class AuthControllerTest {

    @Mock
    private UserService userService;

    @Mock
    private SensorManagerUtil sensorManagerUtil;

    private AuthController authController;

    @BeforeEach
    void setUp() throws Exception {
        try (final AutoCloseable autoCloseable = MockitoAnnotations.openMocks(this)) {
            authController = new AuthController(userService, sensorManagerUtil);
            ReflectionTestUtils.setField(sensorManagerUtil, "tokenSecret", "tokenSecret");
            ReflectionTestUtils.setField(sensorManagerUtil, "tokenAccessExpire", 600000);
            ReflectionTestUtils.setField(sensorManagerUtil, "tokenRefreshExpire", 1800000);
        }
    }

    @Test
    void refreshToken() throws IOException {

        //https://jwt.io/  change signature tokenSecret
        Algorithm algorithm = Algorithm.HMAC256("tokenSecret".getBytes());
        JWTVerifier verifier = JWT.require(algorithm).build();

        Mockito.when(sensorManagerUtil.getToken(Mockito.any())).thenReturn(verifier.verify("eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.dqmo9euORt-zCYxaH-q6hvukkoWPNs4zRSLqTulfzwA"));
        SensorUser sensorUser = new SensorUser();
        sensorUser.setRoles(new ArrayList<>());
        Mockito.when(userService.getByUsername(Mockito.anyString())).thenReturn(sensorUser);
        Mockito.when(sensorManagerUtil.writeTokensResponse(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any())).thenReturn(new HashMap<>());

        authController.refreshToken(new MockHttpServletRequest(), new MockHttpServletResponse());

        Mockito.verify(sensorManagerUtil, Mockito.times(1)).writeTokensResponse(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any());
    }

    @Test
    void getToken() throws IOException {

        SensorUser sensorUser = new SensorUser();
        sensorUser.setRoles(new ArrayList<>());
        Mockito.when(userService.getByUsernameAndPassword(Mockito.anyString(), Mockito.anyString())).thenReturn(sensorUser);
        Mockito.when(sensorManagerUtil.writeTokensResponse(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any())).thenReturn(new HashMap<>());

        LoginSensorUserDTO loginSensorUserDTO = new LoginSensorUserDTO("username", "password");

        authController.getToken(new MockHttpServletRequest(), new MockHttpServletResponse(), loginSensorUserDTO);

        Mockito.verify(sensorManagerUtil, Mockito.times(1)).writeTokensResponse(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any());
    }

    @Test
    void invalidToken() {

        Assertions.assertThrows(RuntimeException.class, () -> authController.validateToken("token"));

    }
}
