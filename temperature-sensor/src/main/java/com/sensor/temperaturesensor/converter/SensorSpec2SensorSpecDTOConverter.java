package com.sensor.temperaturesensor.converter;

import com.sensor.sensorcommon.dto.SensorSpecDTO;
import com.sensor.temperaturesensor.model.SensorSpec;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class SensorSpec2SensorSpecDTOConverter implements Converter<SensorSpec, SensorSpecDTO> {

    @Override
    public @Nullable SensorSpecDTO convert(@NonNull SensorSpec sensorSpec) {

        SensorSpecDTO sensorSpecDTO = new SensorSpecDTO(sensorSpec.getId(), sensorSpec.getName(), sensorSpec.getSensorCategory());
        return sensorSpecDTO;
    }
}
