package com.sensor.temperaturesensor.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.util.Date;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SensorEndpointDTO {

    @NotBlank
    private String userId;

    @NotBlank
    private String sensorId;

    @NotBlank
    private Float value;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
    @NotBlank
    private Date date;

    private OffsetDateTime parsedDateTime;

}
