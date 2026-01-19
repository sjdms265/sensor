package com.sensor.sensormcpserver.service;

import com.sensor.sensormcpserver.dto.GraphSensorEndpoint;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Component
@RequiredArgsConstructor
public class SensorTools {

    private final SensorService sensorService;

    private final HttpServletRequest servletRequest;

    @Tool(name = "get-sensor-info-by-username-and-pattern",
            description = "Filters sensor endpoints by the provided username and pattern")
    public List<GraphSensorEndpoint> sensorEndpointsBy(@ToolParam(description = "The username looked up when filtering") String userName,
                                                       @ToolParam(description = "The pattern looked up when filtering, ex: temperature,humidity") String pattern) {

        List<GraphSensorEndpoint> sensorEndpoints = new ArrayList<>();
        Arrays.stream(pattern.split(",")).forEach(sensorType -> {
            String sensorId = switch (sensorType) {

                //FIXME create a persistence table for specify type of sensor
                case "temperature" -> "sensor.10000db11e_t";
                case "humidity" -> "sensor.10000db11e_h";
                default -> throw new IllegalArgumentException("Invalid sensor type: " + sensorType);

            };

            sensorEndpoints.addAll(sensorService.getSensorEndpointsList(servletRequest, userName, sensorId, 50));
        });

        return sensorEndpoints;
    }
}
