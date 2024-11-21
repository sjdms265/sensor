package com.sensor.temperaturesensor.converter;

import com.sensor.temperaturesensor.dto.SensorEndpointDTO;
import com.sensor.temperaturesensor.model.SensorEndpoint;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.BeanUtils;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

import java.time.ZoneOffset;

@RequiredArgsConstructor
@Component
public class SensorEndpoint2SensorEnpointDTOConverter implements Converter<SensorEndpoint, SensorEndpointDTO> {

    @Override
    public SensorEndpointDTO convert(@NotNull SensorEndpoint sensorEndpoint) {

        SensorEndpointDTO sensorEndpointDTO = SensorEndpointDTO.builder().build();
        BeanUtils.copyProperties(sensorEndpoint, sensorEndpointDTO);
        sensorEndpointDTO.setParsedDateTime(sensorEndpoint.getDate().toInstant()
                .atOffset(ZoneOffset.UTC));
        return sensorEndpointDTO;
    }
}
