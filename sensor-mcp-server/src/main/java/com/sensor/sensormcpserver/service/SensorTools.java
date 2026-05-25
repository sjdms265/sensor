package com.sensor.sensormcpserver.service;

import com.sensor.sensorcommon.dto.GraphSensorEndpoint;
import com.sensor.sensorcommon.dto.HumidexResultDTO;
import com.sensor.sensorcommon.dto.SensorEndpointDTO;
import com.sensor.sensorcommon.dto.SensorSpecDTO;
import com.sensor.sensorcommon.enums.SensorType;
import jakarta.validation.constraints.NotEmpty;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.OptionalDouble;
import java.util.Set;

@Component
@RequiredArgsConstructor
@Slf4j
public class SensorTools {

    @Value("${sensor-mcp-server.pageSize:50}")
    private int pageSize;

    private final SensorService sensorService;

    @Tool(name = "get-sensor-info-by-userId-and-pattern",
            description = "Filters sensor endpoints by the provided userId and pattern")
    public Map<String, List<GraphSensorEndpoint>> sensorEndpointsBy(@ToolParam(description = "The userId looked up when filtering") @NotEmpty String userId,
                                                                    @ToolParam(description = "The pattern looked up when filtering, ex: temperature,humidity") @NotEmpty String pattern,
                                                                    @ToolParam(description = "JWT token") @NotEmpty String token) {

        Map<String, List<GraphSensorEndpoint>> sensorEndpoints = new HashMap<>();
        Set<String> sensorIdsProcessed = new HashSet<>();

        Arrays.stream(pattern.split(",")).forEach(sensorType -> {

            try {

                SensorType sensorTypeEnum = SensorType.valueOf(sensorType.toUpperCase());

                List<SensorEndpointDTO> sensorEndpointDTOS  = sensorService.sensorsByUser(token, userId);

                sensorEndpointDTOS.forEach(sensorEndpointDTO -> {

                    SensorSpecDTO sensorSpec = sensorService.getSensorSpec(sensorEndpointDTO.getSensorId(), token);

                    if(sensorSpec != null && !sensorIdsProcessed.contains(sensorSpec.id()) && sensorSpec.sensorCategory() == sensorTypeEnum) {
                        sensorEndpoints.put(sensorType, sensorService.getSensorEndpointsList(token, userId, sensorSpec.id(), pageSize));
                        sensorIdsProcessed.add(sensorSpec.id());
                    }
                });

            } catch (IllegalArgumentException iae) {
                log.error("Invalid sensor type: {}", sensorType);
            } catch (Exception e) {
                log.error("Error getting sensor endpoints {}", e.getMessage());
            }

        });

        log.info("sensorEndpoints: {}", sensorEndpoints);

        return sensorEndpoints;
    }

    @Tool(name = "get-stats-by-userId-sensorId",
            description = "Filters sensor endpoints by the provided userId and sensorId")
    public List<GraphSensorEndpoint> sensorStatsEndpointsBy(@ToolParam(description = "The userId looked up when filtering") String userId,
            @ToolParam(description = "The sensorId looked up when filtering") String sensorId,
            @ToolParam(description = "Number of record to analyze") Integer pageSize,
            @ToolParam(description = "JWT token") String token) {

        return  sensorService.getSensorEndpointsList(token, userId, sensorId, pageSize);

    }

    @Tool(name = "get-humidex-by-userId-sensorId",
            description = "Computes the Humidex comfort index for a user by fetching the latest temperature and humidity readings. "
                    + "Returns the numeric Humidex value and a comfort level: "
                    + "NO_DISCOMFORT (<29), SOME_DISCOMFORT (30-39), GREAT_DISCOMFORT (40-45), DANGEROUS (45-54), HEAT_STROKE (>54).")
    public HumidexResultDTO computeHumidex(
            @ToolParam(description = "The userId whose sensors are queried") @NotEmpty String userId,
            @ToolParam(description = "JWT token") @NotEmpty String token) {

        List<SensorEndpointDTO> allSensors = sensorService.sensorsByUser(token, userId);

        OptionalDouble temperature = allSensors.stream()
                .filter(s -> {
                    SensorSpecDTO spec = sensorService.getSensorSpec(s.getSensorId(), token);
                    return spec != null && spec.sensorCategory() == SensorType.TEMPERATURE;
                })
                .mapToDouble(s -> s.getValue())
                .average();

        OptionalDouble humidity = allSensors.stream()
                .filter(s -> {
                    SensorSpecDTO spec = sensorService.getSensorSpec(s.getSensorId(), token);
                    return spec != null && spec.sensorCategory() == SensorType.HUMIDITY;
                })
                .mapToDouble(s -> s.getValue())
                .average();

        if (temperature.isEmpty() || humidity.isEmpty()) {
            log.warn("Could not find temperature or humidity sensors for userId={}", userId);
            throw new IllegalStateException(
                    "No temperature or humidity sensor data found for userId: " + userId);
        }

        log.info("Computing Humidex for userId={} temperature={} humidity={}", userId,
                temperature.getAsDouble(), humidity.getAsDouble());

        return HumidexCalculator.calculate(temperature.getAsDouble(), humidity.getAsDouble());
    }
}
