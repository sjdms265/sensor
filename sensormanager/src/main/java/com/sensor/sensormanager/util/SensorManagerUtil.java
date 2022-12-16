package com.sensor.sensormanager.util;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTCreator;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sensor.sensormanager.model.Role;
import com.sensor.sensormanager.model.SensorUser;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Component
@Slf4j
public class SensorManagerUtil {

    private static String tokenSecret;
    private static Integer tokenAccessExpire;
    private static Integer tokenRefreshExpire;

    public static  String createToken(final User user, final String requestUrl, int expirationTime, boolean includeRoles) {

        JWTCreator.Builder jwtBuilder = createCommonTokenBuilder(user.getUsername(), requestUrl, expirationTime);

        if(includeRoles) {
            jwtBuilder.withClaim("roles", user.getAuthorities().stream().map(GrantedAuthority::getAuthority).toList());
        }

        Algorithm algorithm = Algorithm.HMAC256(tokenSecret.getBytes());
        return jwtBuilder.sign(algorithm);
    }


    public static  String createToken(final SensorUser user, final String requestUrl, int expirationTime, boolean includeRoles) {

        JWTCreator.Builder jwtBuilder = createCommonTokenBuilder(user.getUsername(), requestUrl, expirationTime);

        if(includeRoles) {
            jwtBuilder.withClaim("roles", user.getRoles().stream().map(Role::getName).toList());
        }

        Algorithm algorithm = Algorithm.HMAC256(tokenSecret.getBytes());
        return jwtBuilder.sign(algorithm);
    }

    public static JWTCreator.Builder createCommonTokenBuilder(String userName, final String requestUrl, int expirationTime) {

        return JWT.create().withSubject(userName)
                .withExpiresAt(new Date(System.currentTimeMillis()  +expirationTime))
                .withIssuer(requestUrl);
    }

    public static DecodedJWT getToken(HttpServletRequest request) {
        String authorizationHeader = request.getHeader(HttpHeaders.AUTHORIZATION);

        if(authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {

            String token = authorizationHeader.substring("Bearer ".length());
            Algorithm algorithm = Algorithm.HMAC256(tokenSecret.getBytes());
            JWTVerifier verifier = JWT.require(algorithm).build();
            return verifier.verify(token);
        }

        return null;

    }

    public static void setResponseMessage(HttpServletResponse response, Exception e) throws IOException {
        log.error("error logging {}", e.getMessage());

        response.setHeader("error", e.getMessage());
        response.setStatus(HttpStatus.FORBIDDEN.value());

        Map<String, String> error = new HashMap<>();
        error.put("error_message", e.getMessage());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        new ObjectMapper().writeValue(response.getOutputStream(), error);
    }

    @Value("${sensormanager.token.secret}")
    public void setTokenSecret(String tokenSecret) {
        SensorManagerUtil.tokenSecret = tokenSecret;
    }
    @Value("${sensormanager.token.access-expires}")
    public void setTokenAccessExpire(Integer tokenAccessExpire) {
        SensorManagerUtil.tokenAccessExpire = tokenAccessExpire;
    }
    @Value("${sensormanager.token.refresh-expires}")
    public void setTokenRefreshExpire(Integer tokenRefreshExpire) {
        SensorManagerUtil.tokenRefreshExpire = tokenRefreshExpire;
    }

    public static String getTokenSecret() {
        return tokenSecret;
    }

    public static Integer getTokenAccessExpire() {
        return tokenAccessExpire;
    }

    public static Integer getTokenRefreshExpire() {
        return tokenRefreshExpire;
    }

}
