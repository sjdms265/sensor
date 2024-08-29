package com.sensor.sensormanager.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.util.Collection;

/*
    https://stackoverflow.com/questions/62455515/how-can-i-use-java-records-as-dto-with-modelmapper
 */
public record SensorUserDTO(@NotBlank String name, @NotBlank String username, @NotBlank String password,
                            @NotEmpty(message = "Roles are empty") Collection<String> roles) {

}