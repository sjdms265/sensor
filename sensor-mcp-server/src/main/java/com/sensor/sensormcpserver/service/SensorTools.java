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
import java.util.Set;

@Component
@RequiredArgsConstructor
@Slf4j
public class SensorTools {

    @Value("${sensor-mcp-server.pageSize:50}")
    private int pageSize;

    private final SensorService sensorService;

    @Tool(name = "get-sensor-info-by-userId-and-pattern",
            description = "Predicts or retrieves general ambient conditions (like rain probability, general temperature, or humidity) for a user by scanning across sensor type patterns. " +
                    "Use this tool when the user asks about the weather, general environmental conditions, or current status without asking for statistical math calculations." +
                    "Inputs: " +
                    "   userId: The unique identifier of the user." +
                    "   pattern: A text string representing the sensor type or category to match (e.g., temperature). " +
                    "Returns: A JSON object where each key is a matched pattern, containing a list of data points with value and parsedDateTime.")
    public Map<String, List<GraphSensorEndpoint>> sensorEndpointsBy(@ToolParam(description = "The userId looked up when filtering") @NotEmpty String userId,
                                                                    @ToolParam(description = "The pattern looked up when filtering, ex: temperature,humidity") @NotEmpty String pattern,
                                                                    @ToolParam(description = "JWT token") @NotEmpty String token) {

        Map<String, List<GraphSensorEndpoint>> sensorEndpoints = new HashMap<>();
        Set<String> sensorIdsProcessed = new HashSet<>();

        log.info("Getting sensor endpoints for userId {} and pattern {}", userId, pattern);

        Arrays.stream(pattern.split(",")).forEach(sensorType -> {

            try {

                SensorType sensorTypeEnum = SensorType.valueOf(sensorType.toUpperCase());

                log.info("sensorType: {}", sensorTypeEnum);

                List<SensorEndpointDTO> sensorEndpointDTOS  = sensorService.sensorsByUser(token, userId);

                log.info("sensorEndpointDTOs: {}", sensorEndpointDTOS);

                sensorEndpointDTOS.forEach(sensorEndpointDTO -> {

                    SensorSpecDTO sensorSpec = sensorService.getSensorSpec(sensorEndpointDTO.getSensorId(), token);

                    log.info("sensorSpec: {}", sensorSpec);

                    if(sensorSpec != null && !sensorIdsProcessed.contains(sensorSpec.id()) && sensorSpec.sensorCategory() == sensorTypeEnum) {

                        log.info("userId : {}, SensorId {}", userId, sensorEndpointDTO.getSensorId());
                        sensorEndpoints.put(sensorType, sensorService.getSensorEndpointsList(token, userId, sensorEndpointDTO.getSensorId(), pageSize));
                        sensorIdsProcessed.add(sensorSpec.id());
                    }
                });

            } catch (IllegalArgumentException _) {
                log.error("Invalid sensor type: {}", sensorType);
            } catch (Exception e) {
                log.error("Error getting sensor endpoints {}", e.getMessage());
            }

        });

        log.info("sensorEndpoints: {}", sensorEndpoints);

        return sensorEndpoints;
    }

    @Tool(name = "get-stats-by-userId-sensorId",
            description = "Calculates math metrics (average, maximum, minimum) for one specific sensor ID belonging to a user. " +
                    "Use this tool ONLY when the user asks for math calculations, highest/lowest values, averages, or provides a specific sensorId and a history limit (pageSize). " +
                    "Do not use this tool for general weather queries." +
                    "Inputs:" +
                    "   userId: The unique identifier of the user." +
                    "   sensorId: The explicit, unique identifier of a single sensor hardware endpoint." +
                    "   numberOfRecords: The max number of historical database rows to return." +
                    "Returns: A flat JSON list of data points with value and parsedDateTime for the requested sensor.")
    public List<GraphSensorEndpoint> sensorStatsEndpointsBy(@ToolParam(description = "The userId looked up when filtering") String userId,
            @ToolParam(description = "The sensorId looked up when filtering") String sensorId,
            @ToolParam(description = "Number of record to analyze", required = false) Integer pageSize,
            @ToolParam(description = "JWT token") String token) {

        if (pageSize == null) { pageSize = this.pageSize; }

        log.info("sensorStatsEndpointsBy userId {} sensorId {} pageSize {}", userId,  sensorId,  pageSize);

        return  sensorService.getSensorEndpointsList(token, userId, sensorId, pageSize);

    }

    @Tool(name = "get-humidex-by-userId-sensorId",
            description = "Computes the Humidex comfort index for a user by fetching the latest temperature and humidity readings. "
                    + "Returns the numeric Humidex value and a comfort level: "
                    + "NO_DISCOMFORT (<29), SOME_DISCOMFORT (30-39), GREAT_DISCOMFORT (40-45), DANGEROUS (45-54), HEAT_STROKE (>54).")
    public HumidexResultDTO computeHumidex(
            @ToolParam(description = "The userId whose sensors are queried") @NotEmpty String userId,
            @ToolParam(description = "JWT token") @NotEmpty String token) {

        log.info("computeHumidex userId {} ", userId);

        List<SensorEndpointDTO> allSensors = sensorService.sensorsByUser(token, userId);

        List<SensorEndpointDTO> temperatureSensors = allSensors.stream()
                .filter(s -> {
                    SensorSpecDTO spec = sensorService.getSensorSpec(s.getSensorId(), token);
                    return spec != null && spec.sensorCategory() == SensorType.TEMPERATURE;
                })
                .toList();

        List<SensorEndpointDTO> humiditySensors = allSensors.stream()
                .filter(s -> {
                    SensorSpecDTO spec = sensorService.getSensorSpec(s.getSensorId(), token);
                    return spec != null && spec.sensorCategory() == SensorType.HUMIDITY;
                })
                .toList();
                

        if (temperatureSensors.isEmpty() || humiditySensors.isEmpty()) {
            log.warn("Could not find temperature or humidity sensors for userId={}", userId);
            throw new IllegalStateException(
                    "No temperature or humidity sensor data found for userId: " + userId);
        }

        // Fetch the latest reading for the first temperature and humidity sensor found
        List<GraphSensorEndpoint> temperatureGraphSensors = sensorService.getSensorEndpointsList(token, userId, temperatureSensors.getFirst().getSensorId(), 1);
        List<GraphSensorEndpoint> humidityGraphSensors = sensorService.getSensorEndpointsList(token, userId, humiditySensors.getFirst().getSensorId(), 1);

        log.info("Computing Humidex for userId={} temperature={} humidity={}", userId,
                temperatureGraphSensors.getFirst().value(), humidityGraphSensors.getFirst().value());

        return HumidexCalculator.calculate(temperatureGraphSensors.getFirst().value(), humidityGraphSensors.getFirst().value());
    }
}
