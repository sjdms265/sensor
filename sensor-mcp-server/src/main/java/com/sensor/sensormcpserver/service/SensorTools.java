package com.sensor.sensormcpserver.service;

import com.sensor.sensorcommon.dto.GraphSensorEndpoint;
import com.sensor.sensorcommon.dto.SensorEndpointDTO;
import com.sensor.sensorcommon.dto.SensorSpecDTO;
import com.sensor.sensorcommon.enums.SensorType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
        Set<String> sensorIdsProcessed = new HashSet<>();

        Arrays.stream(pattern.split(",")).forEach(sensorType -> {

            try {

                SensorType sensorTypeEnum = SensorType.valueOf(sensorType.toUpperCase());

                List<SensorEndpointDTO> sensorEndpointDTOS  = sensorService.sensorsByUser(token, userId);

                sensorEndpointDTOS.forEach(sensorEndpointDTO -> {

                    SensorSpecDTO sensorSpec = sensorService.getSensorSpec(sensorEndpointDTO.getSensorId(), token);

                    if(!sensorIdsProcessed.contains(sensorSpec.id()) && sensorSpec.sensorCategory() == sensorTypeEnum) {
                        sensorEndpoints.put(sensorType, sensorService.getSensorEndpointsList(token, userId, sensorSpec.id(), 50));
                        sensorIdsProcessed.add(sensorSpec.id());
                    }
                });

            } catch (IllegalArgumentException iae) {
                log.error("Invalid sensor type: {}", sensorType);
            }

        });

        log.info("sensorEndpoints: {}", sensorEndpoints);

        return sensorEndpoints;
    }
}
