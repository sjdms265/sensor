package com.sensor.sensorcommon.dto;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.util.Date;

public record GraphSensorEndpoint(Float value, @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'") Date parsedDateTime) {
}
