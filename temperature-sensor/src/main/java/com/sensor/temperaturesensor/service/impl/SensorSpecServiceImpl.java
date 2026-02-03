package com.sensor.temperaturesensor.service.impl;

import com.sensor.sensorcommon.dto.SensorSpecDTO;
import com.sensor.temperaturesensor.model.SensorSpec;
import com.sensor.temperaturesensor.repository.SensorSpecRepository;
import com.sensor.temperaturesensor.service.SensorSpecService;
import io.micrometer.observation.annotation.Observed;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

@Service
@AllArgsConstructor
@Transactional
@Slf4j
@Observed(name = "SensorSpectServiceImpl")
public class SensorSpecServiceImpl implements SensorSpecService {

    private final SensorSpecRepository sensorSpecRepository;

    private final Converter<SensorSpecDTO, SensorSpec> sensorSpecDTOSensorSpecConverter;
    private final Converter<SensorSpec, SensorSpecDTO> sensorSpecSensorSpecDTOConverter;

    @Override
    public SensorSpecDTO save(SensorSpecDTO sensorSpecDTO) {

        SensorSpec sensorSpec = sensorSpecDTOSensorSpecConverter.convert(sensorSpecDTO);
        log.debug("save {}", sensorSpecDTO);
        return sensorSpecSensorSpecDTOConverter.convert(sensorSpecRepository.save(Objects.requireNonNull(sensorSpec)));

    }

    @Override
    public List<SensorSpecDTO> getSensorSpecs() {
        List<SensorSpec> sensorSpecs = sensorSpecRepository.findAll();
        log.debug("sensorSpecs {}", sensorSpecs);
        return sensorSpecs.stream().map(sensorSpecSensorSpecDTOConverter::convert).toList();
    }

    @Override
    public SensorSpecDTO getBySensorSpecById(String id) {
        SensorSpec sensorSpec = sensorSpecRepository.findSensorSpecById(id);
        log.debug("sensorSpec {}", sensorSpec);
        return sensorSpecSensorSpecDTOConverter.convert(sensorSpec);
    }

    @Override
    public SensorSpecDTO delete(SensorSpecDTO sensorSpecDTO) {
        SensorSpec sensorSpec = sensorSpecDTOSensorSpecConverter.convert(sensorSpecDTO);
        sensorSpecRepository.delete(Objects.requireNonNull(sensorSpec));
        log.debug("sensorSpec {}", sensorSpec);
        return sensorSpecSensorSpecDTOConverter.convert(sensorSpecRepository.save(sensorSpec));
    }


}
