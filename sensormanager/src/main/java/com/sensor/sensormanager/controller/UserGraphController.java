package com.sensor.sensormanager.controller;

import com.sensor.sensormanager.model.SensorUser;
import com.sensor.sensormanager.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class UserGraphController {

    private final UserService userService;

    @QueryMapping
    public List<SensorUser> users() {
        return userService.getUsers();
    }

    @QueryMapping
    public SensorUser userByUserName(@Argument String username) {
        return userService.getByUsername(username);
    }
}
