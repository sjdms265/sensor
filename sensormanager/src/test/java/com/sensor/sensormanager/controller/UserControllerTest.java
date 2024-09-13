package com.sensor.sensormanager.controller;

import com.sensor.sensormanager.dto.RoleToUserForm;
import com.sensor.sensormanager.model.Role;
import com.sensor.sensormanager.model.SensorUser;
import com.sensor.sensormanager.service.UserService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.ArrayList;
import java.util.List;

class UserControllerTest {

    @Mock
    private UserService userService;

    private UserController userController;

    @BeforeEach
    void setUp() throws Exception {
        try (final AutoCloseable autoCloseable = MockitoAnnotations.openMocks(this)) {
            userController = new UserController(userService);
        }
    }

    @Test
    void getUsers() {

        List<SensorUser> users = new ArrayList<>();
        users.add(new SensorUser());
        Mockito.when(userService.getUsers()).thenReturn(users);

        ResponseEntity<List<SensorUser>> responseUsers = userController.getUsers();

        Assertions.assertEquals(HttpStatus.OK, responseUsers.getStatusCode());
        Assertions.assertNotNull(responseUsers);
        Assertions.assertNotNull(responseUsers.getBody());
        Assertions.assertFalse(responseUsers.getBody().isEmpty());
    }

    @Test
    void saveUser() {
        Mockito.when(userService.saveUser(Mockito.any())).thenReturn(new SensorUser());

        ResponseEntity<SensorUser> responseUser = userController.saveUser(new SensorUser());

        Assertions.assertEquals(HttpStatus.CREATED, responseUser.getStatusCode());
        Assertions.assertNotNull(responseUser);
        Assertions.assertNotNull(responseUser.getBody());

    }

    @Test
    void saveRole() {
        Mockito.when(userService.saveRole(Mockito.any())).thenReturn(new Role());

        ResponseEntity<Role> responseRole = userController.saveRole(new Role());

        Assertions.assertEquals(HttpStatus.CREATED, responseRole.getStatusCode());
        Assertions.assertNotNull(responseRole);
        Assertions.assertNotNull(responseRole.getBody());
    }

    @Test
    void addRoleToUser() {
        Mockito.when(userService.saveRole(Mockito.any())).thenReturn(new Role());

        ResponseEntity<Role> responseRole = userController.addRoleToUser(new RoleToUserForm("username", "roleName"));

        Assertions.assertEquals(HttpStatus.OK, responseRole.getStatusCode());
        Assertions.assertNotNull(responseRole);
    }
}
