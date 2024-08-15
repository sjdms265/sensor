package com.sensor.sensormanager.controller;

import com.auth0.jwt.interfaces.DecodedJWT;
import com.sensor.sensormanager.dto.RoleToUserForm;
import com.sensor.sensormanager.model.Role;
import com.sensor.sensormanager.model.SensorUser;
import com.sensor.sensormanager.service.UserService;
import com.sensor.sensormanager.util.SensorManagerUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class UserController {

    private final UserService userService;

    private final SensorManagerUtil sensorManagerUtil;

    @GetMapping("/users")
    public ResponseEntity<List<SensorUser>> getUsers() {
        return ResponseEntity.ok().body(userService.getUsers());
    }

    @PostMapping("/users/save")
    public ResponseEntity<SensorUser> saveUser(@RequestBody SensorUser user) {
        return new ResponseEntity<>(userService.saveUser(user), HttpStatus.CREATED);
    }

    @PostMapping("/roles/save")
    public ResponseEntity<Role> saveRole(@RequestBody Role role) {
        return new ResponseEntity<>(userService.saveRole(role), HttpStatus.CREATED);
    }

    @PostMapping("/roles/addtouser")
    public ResponseEntity<Role> addRoleToUser(@RequestBody RoleToUserForm roleToUserForm) {
        return new ResponseEntity<>(userService.addRoleToUser(roleToUserForm.username(), roleToUserForm.roleName()), HttpStatus.OK);
    }

    @GetMapping("/refreshToken")
    public void refreshToken(HttpServletRequest request, HttpServletResponse response) throws IOException {

        DecodedJWT decodedJWT = sensorManagerUtil.getToken(request);

        if(decodedJWT != null) {

            try {

                String username = decodedJWT.getSubject();

                SensorUser user = userService.getUser(username);

                List<String> claims = user.getRoles().stream().map(Role::getName).toList();

                sensorManagerUtil.writeTokensResponse(request, response, claims, user.getUsername());


            } catch (Exception e) {
                sensorManagerUtil.setResponseMessage(response, e);
            }
        } else {
            throw new IOException("Refresh token missing");
        }
    }

}
