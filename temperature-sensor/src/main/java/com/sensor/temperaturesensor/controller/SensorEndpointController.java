package com.sensor.temperaturesensor.controller;

import com.sensor.temperaturesensor.dto.SensorEndpointDTO;
import com.sensor.temperaturesensor.service.SensorEndpointService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

import java.time.OffsetDateTime;
import java.util.List;

@Controller
@RequiredArgsConstructor
@Slf4j
public class SensorEndpointController {

    private final SensorEndpointService sensorEndpointService;

    @QueryMapping
    public List<SensorEndpointDTO> sensorEndpoints(@Argument String userId, @Argument String sensorId,
                                                   @Argument OffsetDateTime fromDate, @Argument OffsetDateTime toDate,
                                                   @Argument Integer pageNumber, @Argument Integer pageSize) {
        return sensorEndpointService.getByUserIdAndSensorIdAndDate(userId, sensorId, fromDate, toDate, pageNumber, pageSize);
    }
}
