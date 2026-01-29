package com.sensor.temperaturesensor.dto;

import com.sensor.temperaturesensor.enums.SensorType;
import jakarta.validation.constraints.NotBlank;

public record SensorSpecDTO(@NotBlank String id, @NotBlank String name, SensorType sensorCategory){

}
