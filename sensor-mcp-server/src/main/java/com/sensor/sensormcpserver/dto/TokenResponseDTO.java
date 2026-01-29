package com.sensor.sensormcpserver.dto;

import jakarta.validation.constraints.NotBlank;

/*
    https://stackoverflow.com/questions/62455515/how-can-i-use-java-records-as-dto-with-modelmapper
 */
public record TokenResponseDTO(@NotBlank String access_token, @NotBlank String refresh_token) {

}
