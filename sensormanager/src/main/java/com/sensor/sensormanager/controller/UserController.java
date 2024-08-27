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

    public final static String ADMIN_PATH = "/admin";
    public final static String ADMIN_USERS_PATH = ADMIN_PATH + "/users";
    public final static String USERS_PATH = "/users";
    public final static String ADMIN_ROLES_PATH = ADMIN_PATH + "/roles";
    public final static String ROLES_PATH = "/roles";
    public final static String ADD_ROLE_TO_USER_PATH = "/roles/addtouser";
    public final static String REFRESH_TOKEN = "/refreshToken";

    @GetMapping(ADMIN_USERS_PATH)
    public ResponseEntity<List<SensorUser>> getUsers() {
        return ResponseEntity.ok().body(userService.getUsers());
    }

    @DeleteMapping(USERS_PATH + "/{userName}")
    @ResponseStatus(HttpStatus.OK)
    public void deleteUser(@PathVariable String userName) {
        userService.deleteUser(userName);
    }

    @PostMapping(USERS_PATH)
    public ResponseEntity<SensorUser> saveUser(@RequestBody SensorUser user) {
        return new ResponseEntity<>(userService.saveUser(user), HttpStatus.CREATED);
    }

    @PostMapping(ROLES_PATH)
    public ResponseEntity<Role> saveRole(@RequestBody Role role) {
        return new ResponseEntity<>(userService.saveRole(role), HttpStatus.CREATED);
    }

    @PostMapping(ADD_ROLE_TO_USER_PATH)
    public ResponseEntity<Role> addRoleToUser(@RequestBody RoleToUserForm roleToUserForm) {
        return new ResponseEntity<>(userService.addRoleToUser(roleToUserForm.username(), roleToUserForm.roleName()), HttpStatus.OK);
    }

    @DeleteMapping(ROLES_PATH + "/{name}")
    @ResponseStatus(HttpStatus.OK)
    public void deleteRole(@PathVariable String name) {
        userService.deleteRole(name);
    }

    @GetMapping(REFRESH_TOKEN)
    public void refreshToken(HttpServletRequest request, HttpServletResponse response) throws IOException {

        DecodedJWT decodedJWT = sensorManagerUtil.getToken(request);

        if(decodedJWT != null) {

            try {

                String username = decodedJWT.getSubject();

                SensorUser user = userService.getByUsername(username);

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
