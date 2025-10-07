package com.sensor.sensormanager.controller;

import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.sensor.sensormanager.dto.LoginSensorUserDTO;
import com.sensor.sensormanager.model.Role;
import com.sensor.sensormanager.model.SensorUser;
import com.sensor.sensormanager.service.UserService;
import com.sensor.sensormanager.util.SensorManagerUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.sensor.sensormanager.controller.BaseController.AUTH_PATH;

@RestController
@RequiredArgsConstructor
@RequestMapping(AUTH_PATH)
@Slf4j
public class AuthController {

    public static final String ERROR = "error";
    private final UserService userService;

    private final SensorManagerUtil sensorManagerUtil;

    public static final String REFRESH_TOKEN = "/refreshToken";
    public static final String GET_TOKEN = "/token";
    public static final String VALIDATE_TOKEN = "/validateToken";

    @GetMapping(REFRESH_TOKEN)
    public Map<String, String> refreshToken(HttpServletRequest request, HttpServletResponse response) throws IOException {

        Map<String, String> model = new HashMap<>();
        DecodedJWT decodedJWT = sensorManagerUtil.getToken(request);

        if(decodedJWT != null) {

            try {

                String username = decodedJWT.getSubject();

                SensorUser user = userService.getByUsername(username);

                List<String> claims = user.getRoles().stream().map(Role::getName).toList();

                model = sensorManagerUtil.writeTokensResponse(request, response, claims, user.getUsername());


            } catch (Exception e) {
                sensorManagerUtil.setResponseMessage(response, e);
                model.put(ERROR, e.getMessage());
            }
        } else {
            log.error("Refresh token missing");
            model.put(ERROR, "Refresh token missing");
        }

        return model;
    }

    @PostMapping(GET_TOKEN)
    public Map<String, String> postToken(HttpServletRequest request, HttpServletResponse response,
                                        @RequestBody LoginSensorUserDTO loginUser) throws IOException {
        return getToken(request,response,loginUser);
    }

    @GetMapping(GET_TOKEN)
    public Map<String, String> getToken(HttpServletRequest request, HttpServletResponse response,
                                        @RequestBody LoginSensorUserDTO loginUser) throws IOException {

        Map<String, String> model = new HashMap<>();
        try {
            SensorUser user = userService.getByUsernameAndPassword(loginUser.username(), loginUser.password());

            if (user != null) {
                List<String> claims = user.getRoles().stream().map(Role::getName).toList();
                model = sensorManagerUtil.writeTokensResponse(request, response, claims, user.getUsername());
            } else {
                sensorManagerUtil.setResponseMessage(response, new Exception("user doesn't exist " + loginUser.username()));
                model.put(ERROR, "user doesn't exist " + loginUser.username());
            }

        } catch (Exception e) {
            sensorManagerUtil.setResponseMessage(response, e);
            model.put(ERROR, e.getMessage());
        }

        return model;
    }

    @GetMapping(VALIDATE_TOKEN + "/{token}")
    public Map<String, String> validateToken(@PathVariable(value = "token") String token) {

        Map<String, String> model = new HashMap<>();

        try {
            DecodedJWT decodedJWT = sensorManagerUtil.validateToken(token);
            if(decodedJWT == null) {
                log.error("Token invalid {}", token);
                model.put(ERROR, "invalid token " + token);
                throw new JWTVerificationException("Token invalid " + token);
            } else {
                log.debug("Token valid {}", decodedJWT.getToken());
                model.put("success", "valid token " + token);
            }
        } catch (Exception e) {
            log.error("Token invalid {} error {}", token, e.getMessage());
            throw new JWTVerificationException("Token invalid " + token);
        }
        return model;
    }

}
