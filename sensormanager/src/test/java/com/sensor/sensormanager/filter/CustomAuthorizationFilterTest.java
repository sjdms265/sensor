package com.sensor.sensormanager.filter;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.sensor.sensormanager.util.SensorManagerUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.io.IOException;

class CustomAuthorizationFilterTest {

    @Mock
    private SensorManagerUtil sensorManagerUtil;

    private CustomAuthorizationFilter customAuthorizationFilter;

    @BeforeEach
    void setUp() throws Exception {

        try (final AutoCloseable autoCloseable = MockitoAnnotations.openMocks(this)) {
            customAuthorizationFilter = new CustomAuthorizationFilter(sensorManagerUtil);
        }
    }

    @Test
    void doFilterInternal() throws ServletException, IOException {

        HttpServletRequest request = new MockHttpServletRequest();
        HttpServletResponse response = new MockHttpServletResponse();
        FilterChain filterChain = new MockFilterChain();

        //https://jwt.io/ change signature tokenSecret
        /*{
            "sub": "1234567890",
                "name": "John Doe",
                "iat": 1516239022,
                "roles": [
                        "ADMIN"
              ]
        }*/
        Algorithm algorithm = Algorithm.HMAC256("tokenSecret".getBytes());
        JWTVerifier verifier = JWT.require(algorithm).build();

        Mockito.when(sensorManagerUtil.getToken(Mockito.any())).thenReturn(verifier.verify("eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyLCJyb2xlcyI6WyJBRE1JTiJdfQ.2lHl2S0loGrvAvo7qh61G2rTruvlP4HKG0jX9Z4-LQk"));

        customAuthorizationFilter.doFilterInternal(request, response , filterChain);

        Mockito.verify(sensorManagerUtil, Mockito.times(1)).getToken(Mockito.any());
    }

    @Test
    void doFilterInternalTokenNoRoles() throws ServletException, IOException {

        HttpServletRequest request = new MockHttpServletRequest();
        HttpServletResponse response = new MockHttpServletResponse();
        FilterChain filterChain = new MockFilterChain();

        //https://jwt.io/ change signature tokenSecret
        /*{
            "sub": "1234567890",
                "name": "John Doe",
                "iat": 1516239022
              ]
        }*/
        Algorithm algorithm = Algorithm.HMAC256("tokenSecret".getBytes());
        JWTVerifier verifier = JWT.require(algorithm).build();

        Mockito.when(sensorManagerUtil.getToken(Mockito.any())).thenReturn(verifier.verify("eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.dqmo9euORt-zCYxaH-q6hvukkoWPNs4zRSLqTulfzwA"));

        customAuthorizationFilter.doFilterInternal(request, response , filterChain);

        Mockito.verify(sensorManagerUtil, Mockito.times(1)).getToken(Mockito.any());
        Mockito.verify(sensorManagerUtil, Mockito.times(1)).setResponseMessage(Mockito.any(), Mockito.any());
    }

    @Test
    void doFilterInternalLogin() throws ServletException, IOException {

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setServletPath("/login");
        HttpServletResponse response = new MockHttpServletResponse();
        FilterChain filterChain = new MockFilterChain();

        customAuthorizationFilter.doFilterInternal(request, response , filterChain);

        Mockito.verify(sensorManagerUtil, Mockito.times(0)).getToken(Mockito.any());
    }

    @Test
    void doFilterInternalRefreshToken() throws ServletException, IOException {

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setServletPath("/api/refreshToken");
        HttpServletResponse response = new MockHttpServletResponse();
        FilterChain filterChain = new MockFilterChain();

        customAuthorizationFilter.doFilterInternal(request, response , filterChain);

        Mockito.verify(sensorManagerUtil, Mockito.times(0)).getToken(Mockito.any());
    }

    @Test
    void doFilterInternalJWTNull() throws ServletException, IOException {

        MockHttpServletRequest request = new MockHttpServletRequest();
        HttpServletResponse response = new MockHttpServletResponse();
        FilterChain filterChain = new MockFilterChain();

        Mockito.when(sensorManagerUtil.getToken(Mockito.any())).thenReturn(null);


        customAuthorizationFilter.doFilterInternal(request, response , filterChain);

        Mockito.verify(sensorManagerUtil, Mockito.times(1)).getToken(Mockito.any());
        Mockito.verify(sensorManagerUtil, Mockito.times(0)).setResponseMessage(Mockito.any(), Mockito.any());
    }
}