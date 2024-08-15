package com.sensor.sensormanager.util;

import com.auth0.jwt.interfaces.DecodedJWT;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class SensorManagerUtilTest {

    private SensorManagerUtil sensorManagerUtil;

    @BeforeEach
    void setUp() throws Exception {
        try (final AutoCloseable autoCloseable = MockitoAnnotations.openMocks(this)) {
            sensorManagerUtil = new SensorManagerUtil();
            ReflectionTestUtils.setField(sensorManagerUtil, "tokenSecret", "tokenSecret");
            ReflectionTestUtils.setField(sensorManagerUtil, "tokenAccessExpire", 600000);
            ReflectionTestUtils.setField(sensorManagerUtil, "tokenRefreshExpire", 1800000);
        }
    }

    @Test
    void getToken() {

        //https://jwt.io/ change signature tokenSecret
        /*{
            "sub": "1234567890",
                "name": "John Doe",
                "iat": 1516239022,
                "roles": [
                        "ADMIN"
              ]
        }*/

        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getHeader(HttpHeaders.AUTHORIZATION)).thenReturn("Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyLCJyb2xlcyI6WyJBRE1JTiJdfQ.2lHl2S0loGrvAvo7qh61G2rTruvlP4HKG0jX9Z4-LQk");

        DecodedJWT decodedJWT = sensorManagerUtil.getToken(request);
        Assertions.assertNotNull(decodedJWT);
    }

    @Test
    void getTokenAuthorizationHeaderNull() {

        HttpServletRequest request = mock(HttpServletRequest.class);

        DecodedJWT decodedJWT = sensorManagerUtil.getToken(request);
        Assertions.assertNull(decodedJWT);
    }

    @Test
    void getTokenAuthorizationNoBearer() {

        //https://jwt.io/ change signature tokenSecret
        /*{
            "sub": "1234567890",
                "name": "John Doe",
                "iat": 1516239022,
                "roles": [
                        "ADMIN"
              ]
        }*/

        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getHeader(HttpHeaders.AUTHORIZATION)).thenReturn("eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyLCJyb2xlcyI6WyJBRE1JTiJdfQ.2lHl2S0loGrvAvo7qh61G2rTruvlP4HKG0jX9Z4-LQk");

        DecodedJWT decodedJWT = sensorManagerUtil.getToken(request);
        Assertions.assertNull(decodedJWT);
    }

    @Test
    void writeTokensResponse() throws IOException {

        MockHttpServletRequest request = new MockHttpServletRequest("POST", "");
        HttpServletResponse response = new MockHttpServletResponse();
        List<String> claims = List.of("ADMIN");
        Map<String, String> tokens = sensorManagerUtil.writeTokensResponse(request, response, claims, "username");

        Assertions.assertNotNull(tokens);
        Assertions.assertEquals(2, tokens.size());
    }

    @Test
    void setResponseMessage() throws IOException {

        HttpServletResponse response = new MockHttpServletResponse();
        Map<String, String> error = sensorManagerUtil.setResponseMessage(response, new IOException());

        Assertions.assertNotNull(error);
        Assertions.assertEquals(1, error.size());
    }

}