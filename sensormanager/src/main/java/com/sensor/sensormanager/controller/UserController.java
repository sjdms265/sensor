package com.sensor.sensormanager.controller;

import com.sensor.sensormanager.dto.RoleToUserForm;
import com.sensor.sensormanager.model.Role;
import com.sensor.sensormanager.model.SensorUser;
import com.sensor.sensormanager.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static com.sensor.sensormanager.controller.BaseController.ADMIN_PATH;

@RestController
@RequiredArgsConstructor
@RequestMapping(BaseController.BASE_PATH)
@Slf4j
public class UserController {

    private final UserService userService;

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

}
