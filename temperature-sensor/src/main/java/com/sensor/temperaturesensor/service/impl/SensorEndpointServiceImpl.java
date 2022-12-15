package com.sensor.temperaturesensor.service.impl;

import com.sensor.temperaturesensor.model.SensorEndpoint;
import com.sensor.temperaturesensor.repository.SensorEndpointRepository;
import com.sensor.temperaturesensor.service.SensorEndpointService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;

@Service
@Slf4j
@RequiredArgsConstructor
public class SensorEndpointServiceImpl implements SensorEndpointService {

    private final SensorEndpointRepository sensorEndpointRepository;

    @Override
    public SensorEndpoint save(SensorEndpoint sensorEndpoint) {
        return sensorEndpointRepository.save(sensorEndpoint);
    }

    @Override
    @Async("threadPoolTaskExecutor")
    public Future<SensorEndpoint> recordSensorEndpoint(SensorEndpoint sensorEndpoint) {

        Date lastSensorEndpoint = sensorEndpointRepository.
                getLastSavedSensorEndpoint(sensorEndpoint.getUserId(), sensorEndpoint.getSensorId());

        if(lastSensorEndpoint != null && lastSensorEndpoint.compareTo(sensorEndpoint.getDate()) < 1) {
            SensorEndpoint currentSensorEndpoint = sensorEndpointRepository.
                    getSensorEndpointByUserIdAndSensorIdAndDate(sensorEndpoint.getUserId(), sensorEndpoint.getSensorId(),
                            lastSensorEndpoint);
            if(!currentSensorEndpoint.getValue().equals(sensorEndpoint.getValue())) {
                log.info("Updated SensorEndpointDTO {}", sensorEndpoint);
                save(sensorEndpoint);
                return CompletableFuture.completedFuture(currentSensorEndpoint);
            }
        } else if(lastSensorEndpoint == null) {
            log.info("New SensorEndpointDTO {}", sensorEndpoint);
            save(sensorEndpoint);
            return CompletableFuture.completedFuture(sensorEndpoint);
        }
        return null;
    }
}
