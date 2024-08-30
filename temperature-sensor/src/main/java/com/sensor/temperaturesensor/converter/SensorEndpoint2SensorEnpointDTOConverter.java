package com.sensor.temperaturesensor.converter;

import com.sensor.temperaturesensor.dto.SensorEndpointDTO;
import com.sensor.temperaturesensor.model.SensorEndpoint;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.modelmapper.ModelMapper;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

import java.time.ZoneOffset;

@RequiredArgsConstructor
@Component
public class SensorEndpoint2SensorEnpointDTOConverter implements Converter<SensorEndpoint, SensorEndpointDTO> {

    private final ModelMapper modelMapper;

    @Override
    public SensorEndpointDTO convert(@NotNull SensorEndpoint sensorEndpoint) {

        SensorEndpointDTO sensorEndpointDTO = modelMapper.map(sensorEndpoint, SensorEndpointDTO.class);
        sensorEndpointDTO.setParsedDateTime(sensorEndpoint.getDate().toInstant()
                .atOffset(ZoneOffset.UTC));
        return sensorEndpointDTO;
    }
}
