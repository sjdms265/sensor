package com.sensor.temperaturesensor.converter;

import com.sensor.temperaturesensor.dto.GraphSensorEndpoint;
import com.sensor.temperaturesensor.dto.SensorEndpointDTO;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class SensorEndpointDTO2GraphSensorEndpointConverter implements Converter<SensorEndpointDTO, GraphSensorEndpoint> {

    @Override
    public GraphSensorEndpoint convert(@NotNull SensorEndpointDTO sensorEndpoint) {

       return new GraphSensorEndpoint(sensorEndpoint.getValue(), sensorEndpoint.getDate());

    }
}
