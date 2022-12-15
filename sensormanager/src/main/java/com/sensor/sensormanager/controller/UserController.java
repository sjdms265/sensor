package com.sensor.sensormanager.controller;

import com.auth0.jwt.interfaces.DecodedJWT;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sensor.sensormanager.dto.RoleToUserForm;
import com.sensor.sensormanager.model.Role;
import com.sensor.sensormanager.model.SensorUser;
import com.sensor.sensormanager.service.UserService;
import com.sensor.sensormanager.util.SensorManagerUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.io.IOException;
import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class UserController {


    private final UserService userService;

    @GetMapping("/users")
    public ResponseEntity<List<SensorUser>> getUsers() {
        return ResponseEntity.ok().body(userService.getUsers());
    }

    @PostMapping("/user/save")
    public ResponseEntity<SensorUser> saveUser(@RequestBody SensorUser user) {
        URI location = URI.create(ServletUriComponentsBuilder.fromCurrentContextPath().path("/api/user/save").toUriString());
        return ResponseEntity.created(location).body(userService.saveUser(user));
    }

    @PostMapping("/role/save")
    public ResponseEntity<Role> saveUser(@RequestBody Role role) {
        URI location = URI.create(ServletUriComponentsBuilder.fromCurrentContextPath().path("/api/role/save").toUriString());
        return ResponseEntity.created(location).body(userService.saveRole(role));
    }

    @PostMapping("/role/addtouser")
    public ResponseEntity<URI> addRoleToUser(@RequestBody RoleToUserForm roleToUserForm) {
        URI location = URI.create(ServletUriComponentsBuilder.fromCurrentContextPath().path("/api/role/addtouser").toUriString());
        userService.addRoleToUser(roleToUserForm.username(), roleToUserForm.roleName());
        return ResponseEntity.created(location).build();
    }

    @GetMapping("/refreshToken")
    public void refreshToken(HttpServletRequest request, HttpServletResponse response) throws IOException {

        DecodedJWT decodedJWT = SensorManagerUtil.getToken(request);

        if(decodedJWT != null) {

            try {

                String username = decodedJWT.getSubject();

                SensorUser user = userService.getUser(username);

                String accessToken = SensorManagerUtil.createToken(user, request.getRequestURL().toString(),
                        Integer.parseInt(SensorManagerUtil.getProperty(SensorManagerUtil.SENSORMANAGER_TOKEN_ACCESS_EXPIRES)), true);

                Map<String, String> tokens = new HashMap<>();
                tokens.put("access_token", accessToken);
                //tokens.put("refresh_token", refreshToken); //fixme
                response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                new ObjectMapper().writeValue(response.getOutputStream(), tokens);


            } catch (Exception e) {
                SensorManagerUtil.setResponseMessage(response, e);
            }
        } else {
            throw new IOException("Refresh token missing");
        }
    }
}
