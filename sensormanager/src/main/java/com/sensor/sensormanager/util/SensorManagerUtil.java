package com.sensor.sensormanager.util;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTCreator;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@Slf4j
public class SensorManagerUtil {

    @Getter
    private static String tokenSecret;
    @Getter
    private static Integer tokenAccessExpire;
    @Getter
    private static Integer tokenRefreshExpire;

    private String createToken(final String userName, List<String> claims, final String requestUrl, int expirationTime, boolean includeRoles) {

        JWTCreator.Builder jwtBuilder = createCommonTokenBuilder(userName, requestUrl, expirationTime);

        if(includeRoles) {
            jwtBuilder.withClaim("roles", claims);
        }

        Algorithm algorithm = Algorithm.HMAC256(tokenSecret.getBytes());
        return jwtBuilder.sign(algorithm);
    }

    private JWTCreator.Builder createCommonTokenBuilder(String userName, final String requestUrl, int expirationTime) {

        return JWT.create().withSubject(userName)
                .withExpiresAt(new Date(System.currentTimeMillis() + expirationTime))
                .withIssuer(requestUrl);
    }

    public DecodedJWT getToken(HttpServletRequest request) {
        String authorizationHeader = request.getHeader(HttpHeaders.AUTHORIZATION);

        if(authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {

            String token = authorizationHeader.substring("Bearer ".length());
            Algorithm algorithm = Algorithm.HMAC256(tokenSecret.getBytes());
            JWTVerifier verifier = JWT.require(algorithm).build();
            return verifier.verify(token);
        }

        return null;

    }

    public Map<String, String> writeTokensResponse(HttpServletRequest request, HttpServletResponse response, List<String> claims, String username) throws IOException {
        String accessToken = createToken(username, claims, request.getRequestURL().toString(),
                getTokenAccessExpire(), true);
        String refreshToken = createToken(username, claims, request.getRequestURL().toString(),
                getTokenRefreshExpire(), false);

        Map<String, String> tokens = new HashMap<>();
        tokens.put("access_token", accessToken);
        tokens.put("refresh_token", refreshToken);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        new ObjectMapper().writeValue(response.getOutputStream(), tokens);

        return tokens;
    }

    public Map<String, String> setResponseMessage(HttpServletResponse response, Exception e) throws IOException {
        log.error("error logging {}", e.getMessage());

        response.setHeader("error", e.getMessage());
        response.setStatus(HttpStatus.FORBIDDEN.value());

        Map<String, String> error = new HashMap<>();
        error.put("error_message", e.getMessage());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        new ObjectMapper().writeValue(response.getOutputStream(), error);

        return error;
    }

    @Value("${sensor-manager.token.secret}")
    public void setTokenSecret(String tokenSecret) {
        SensorManagerUtil.tokenSecret = tokenSecret;
    }
    @Value("${sensor-manager.token.access-expires}")
    public void setTokenAccessExpire(Integer tokenAccessExpire) {
        SensorManagerUtil.tokenAccessExpire = tokenAccessExpire;
    }
    @Value("${sensor-manager.token.refresh-expires}")
    public void setTokenRefreshExpire(Integer tokenRefreshExpire) {
        SensorManagerUtil.tokenRefreshExpire = tokenRefreshExpire;
    }

}
