package com.sensor.sensormanager.controller;

import com.sensor.sensormanager.dto.SensorUserDTO;
import com.sensor.sensormanager.model.SensorUser;
import com.sensor.sensormanager.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.convert.converter.Converter;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;

import java.util.List;

@Controller
@RequiredArgsConstructor
@Slf4j
@Validated
public class UserGraphController {

    private final UserService userService;

    private final Converter<SensorUserDTO, SensorUser> sensorUserDTO2SensorUserConverter;

    @QueryMapping
    public List<SensorUser> users() {
        return userService.getUsers();
    }

    @QueryMapping
    public SensorUser userByUserName(@Argument String username) {
        return userService.getByUsername(username);
    }

    @MutationMapping
    public SensorUser saveSensorUser(@Valid @Argument SensorUserDTO sensorUserDTO) {

        log.debug("saveSensorUser {}", sensorUserDTO);
        SensorUser user = sensorUserDTO2SensorUserConverter.convert(sensorUserDTO);

        return userService.saveUser(user);
    }

    @MutationMapping
    public SensorUser deleteUser(@Argument String username) {

        log.debug("deleteUser {}", username);
        return userService.deleteUser(username);
    }
}
