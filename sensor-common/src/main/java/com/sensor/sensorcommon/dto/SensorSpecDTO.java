package com.sensor.sensorcommon.dto;

import com.sensor.sensorcommon.enums.SensorType;
import jakarta.validation.constraints.NotBlank;

public record SensorSpecDTO(@NotBlank String id, @NotBlank String name, SensorType sensorCategory){

}
