package com.sensor.sensormcpserver.service;

import com.sensor.sensormcpserver.dto.SensorEndpointDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class SensorTools {

    private final SensorService sensorService;

    @Tool(name = "get-sensor-info-by-username-andppattern",
            description = "Filters sensor endpoints by the provided username and pattern")
    public List<SensorEndpointDTO> sensorEndpointsBy(@ToolParam(description = "The username looked up when filtering") String userName,
                                                     @ToolParam(description = "The pattern looked up when filtering") String pattern) {
        return sensorService.findByPattern(pattern);
    }
}
