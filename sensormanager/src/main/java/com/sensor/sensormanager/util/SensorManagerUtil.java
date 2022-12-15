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
import org.springframework.context.EnvironmentAware;
import org.springframework.core.env.Environment;
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
public class SensorManagerUtil implements EnvironmentAware {

    public static final String SENSORMANAGER_TOKEN_SECRET = "sensormanager.token.secret";
    public static final String SENSORMANAGER_TOKEN_ACCESS_EXPIRES = "sensormanager.token.access-expires";
    public static final String SENSORMANAGER_TOKEN_REFRESH_EXPIRES = "sensormanager.token.refresh-expires";

    private static Environment env;

    public static String getProperty(String key) {
        return  env.getProperty(key);
    }

    public static  String createToken(final User user, final String requestUrl, int expirationTime, boolean includeRoles) {

        JWTCreator.Builder jwtBuilder = createCommonTokenBuilder(user.getUsername(), requestUrl, expirationTime);

        if(includeRoles) {
            jwtBuilder.withClaim("roles", user.getAuthorities().stream().map(GrantedAuthority::getAuthority).toList());
        }

        Algorithm algorithm = Algorithm.HMAC256(SensorManagerUtil.getProperty(SENSORMANAGER_TOKEN_SECRET).getBytes());
        return jwtBuilder.sign(algorithm);
    }


    public static  String createToken(final SensorUser user, final String requestUrl, int expirationTime, boolean includeRoles) {

        JWTCreator.Builder jwtBuilder = createCommonTokenBuilder(user.getUsername(), requestUrl, expirationTime);

        if(includeRoles) {
            jwtBuilder.withClaim("roles", user.getRoles().stream().map(Role::getName).toList());
        }

        Algorithm algorithm = Algorithm.HMAC256(SensorManagerUtil.getProperty(SENSORMANAGER_TOKEN_SECRET).getBytes());
        return jwtBuilder.sign(algorithm);
    }

    public static JWTCreator.Builder createCommonTokenBuilder(String userName, final String requestUrl, int expirationTime) {


        JWTCreator.Builder jwtBuilder = JWT.create().withSubject(userName)
                .withExpiresAt(new Date(System.currentTimeMillis()  +expirationTime))
                .withIssuer(requestUrl);
        return jwtBuilder;
    }

    public static DecodedJWT getToken(HttpServletRequest request) {
        String authorizationHeader = request.getHeader(HttpHeaders.AUTHORIZATION);

        if(authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {

            String token = authorizationHeader.substring("Bearer ".length());
            Algorithm algorithm = Algorithm.HMAC256(SensorManagerUtil.getProperty(SENSORMANAGER_TOKEN_SECRET).getBytes());
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

    @Override
    public void setEnvironment(Environment environment) {
        this.env = environment;
    }


}
