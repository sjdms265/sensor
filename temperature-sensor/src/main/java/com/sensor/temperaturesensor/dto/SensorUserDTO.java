package com.sensor.temperaturesensor.dto;

import jakarta.validation.constraints.NotBlank;

/*
    https://stackoverflow.com/questions/62455515/how-can-i-use-java-records-as-dto-with-modelmapper
 */
public record SensorUserDTO(@NotBlank String name, @NotBlank String username, @NotBlank String password) {

}