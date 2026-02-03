package com.sensor.temperaturesensor.converter;

import com.sensor.sensorcommon.dto.SensorSpecDTO;
import com.sensor.temperaturesensor.model.SensorSpec;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.beans.BeanUtils;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class SensorSpecDTO2SensorSpecConverter implements Converter<SensorSpecDTO, SensorSpec> {

    @Override
    public @Nullable SensorSpec convert(@NonNull SensorSpecDTO sensorSpecDTO) {

        SensorSpec sensorSpec = SensorSpec.builder().build();
        BeanUtils.copyProperties(sensorSpecDTO, sensorSpec);
        return sensorSpec;
    }
}
