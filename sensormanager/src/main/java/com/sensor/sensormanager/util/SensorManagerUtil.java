package com.sensor.sensormanager.util;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTCreator;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
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

    public static final String BEARER = "Bearer ";
    @Value("${sensor-manager.token.secret}")
    private String tokenSecret;
    @Value("${sensor-manager.token.access-expires}")
    private Integer tokenAccessExpire;
    @Value("${sensor-manager.token.refresh-expires}")
    private Integer tokenRefreshExpire;

    public String createToken(final String userName, List<String> claims, final String requestUrl, int expirationTime, boolean includeRoles) {

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

        if(authorizationHeader != null && authorizationHeader.startsWith(BEARER)) {
            String token = authorizationHeader.substring(BEARER.length());
            return validateToken(token);
        }

        return null;

    }

    public DecodedJWT validateToken(String token) {

        log.debug("validateToken {}", token);
        token = token.replace(BEARER, "");
        Algorithm algorithm = Algorithm.HMAC256(tokenSecret.getBytes());
        JWTVerifier verifier = JWT.require(algorithm).build();

        DecodedJWT jwt = verifier.verify(token);
        log.debug("validateToken payload {}", jwt.getPayload());

        return jwt;

    }

    public Map<String, String> writeTokensResponse(HttpServletRequest request, HttpServletResponse response, List<String> claims, String username) throws IOException {
        String accessToken = createToken(username, claims, request.getRequestURL().toString(),
                this.tokenAccessExpire, true);
        String refreshToken = createToken(username, claims, request.getRequestURL().toString(),
                this.tokenRefreshExpire, false);

        Map<String, String> tokens = new HashMap<>();
        tokens.put("access_token", accessToken);
        tokens.put("refresh_token", refreshToken);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        new ObjectMapper().writeValue(response.getOutputStream(), tokens);

        return tokens;
    }

    public Map<String, String> setResponseMessage(HttpServletResponse response, Exception e) throws IOException {
        log.error("error logging {}", e.getMessage());

        response.setStatus(HttpStatus.FORBIDDEN.value());

        Map<String, String> error = new HashMap<>();
        error.put("error_message", e.getMessage());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        new ObjectMapper().writeValue(response.getOutputStream(), error);

        return error;
    }

}
