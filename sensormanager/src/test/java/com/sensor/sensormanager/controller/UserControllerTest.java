package com.sensor.sensormanager.controller;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.sensor.sensormanager.dto.RoleToUserForm;
import com.sensor.sensormanager.model.Role;
import com.sensor.sensormanager.model.SensorUser;
import com.sensor.sensormanager.service.UserService;
import com.sensor.sensormanager.util.SensorManagerUtil;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

class UserControllerTest {

    @Mock
    private UserService userService;

    @Mock
    private SensorManagerUtil sensorManagerUtil;

    private UserController userController;

    @BeforeEach
    void setUp() throws Exception {
        try (final AutoCloseable autoCloseable = MockitoAnnotations.openMocks(this)) {
            userController = new UserController(userService, sensorManagerUtil);
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

    @Test
    void refreshToken() throws IOException {

        //https://jwt.io/
        Algorithm algorithm = Algorithm.HMAC256("tokenSecret".getBytes());
        JWTVerifier verifier = JWT.require(algorithm).build();

        Mockito.when(sensorManagerUtil.getToken(Mockito.any())).thenReturn(verifier.verify("eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.dqmo9euORt-zCYxaH-q6hvukkoWPNs4zRSLqTulfzwA"));
        SensorUser sensorUser = new SensorUser();
        sensorUser.setRoles(new ArrayList<>());
        Mockito.when(userService.getUser(Mockito.anyString())).thenReturn(sensorUser);
        Mockito.doNothing().when(sensorManagerUtil).writeTokensResponse(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any());

        userController.refreshToken(new MockHttpServletRequest(), new MockHttpServletResponse());

        Mockito.verify(sensorManagerUtil, Mockito.times(1)).writeTokensResponse(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any());
    }
}