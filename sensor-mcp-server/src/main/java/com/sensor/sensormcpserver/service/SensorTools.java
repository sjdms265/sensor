package com.sensor.sensormcpserver.service;

import com.sensor.sensormcpserver.dto.GraphSensorEndpoint;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class SensorTools {

    private final SensorService sensorService;

    @Tool(name = "get-sensor-info-by-userId-and-pattern",
            description = "Filters sensor endpoints by the provided userId and pattern")
    public Map<String, List<GraphSensorEndpoint>> sensorEndpointsBy(@ToolParam(description = "The userId looked up when filtering") String userId,
                                                                    @ToolParam(description = "The pattern looked up when filtering, ex: temperature,humidity") String pattern,
                                                                    @ToolParam(description = "JWT token") String token) {

        Map<String, List<GraphSensorEndpoint>> sensorEndpoints = new HashMap<>();
        Arrays.stream(pattern.split(",")).forEach(sensorType -> {
            String sensorId = switch (sensorType) {

                //FIXME create a persistence table for specify type of sensor
                case "temperature" -> "sensor.10000db11e_t";
                case "humidity" -> "sensor.10000db11e_h";
                default -> throw new IllegalArgumentException("Invalid sensor type: " + sensorType);

            };

            sensorEndpoints.put(sensorType, sensorService.getSensorEndpointsList(token, userId, sensorId, 50));
        });

        log.info("sensorEndpoints: {}", sensorEndpoints);

        return sensorEndpoints;
    }
}
