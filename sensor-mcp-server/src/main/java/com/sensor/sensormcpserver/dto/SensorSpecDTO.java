package com.sensor.sensormcpserver.dto;


import com.sensor.sensormcpserver.enums.SensorType;
import jakarta.validation.constraints.NotBlank;

public record SensorSpecDTO(@NotBlank String id, @NotBlank String name, SensorType sensorCategory){

}
