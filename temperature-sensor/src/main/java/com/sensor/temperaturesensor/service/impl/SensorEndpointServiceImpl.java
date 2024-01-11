package com.sensor.temperaturesensor.service.impl;

import com.sensor.temperaturesensor.model.SensorEndpoint;
import com.sensor.temperaturesensor.repository.SensorEndpointRepository;
import com.sensor.temperaturesensor.service.SensorEndpointService;
import io.micrometer.observation.annotation.Observed;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;

@Service
@Slf4j
@RequiredArgsConstructor
@Observed(name = "SensorEndpointServiceImpl")
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
            List<SensorEndpoint> currentSensorEndpoints = sensorEndpointRepository.
                    getSensorEndpointsByUserIdAndSensorIdAndDate(sensorEndpoint.getUserId(), sensorEndpoint.getSensorId(),
                            lastSensorEndpoint);

            if(currentSensorEndpoints.size() > 1) {
                log.error("Duplicate sensor endpoints {} size {}", currentSensorEndpoints.get(0), currentSensorEndpoints.size());
            } else if(currentSensorEndpoints.isEmpty()) {
                log.error("No sensor endpoint for getSensorEndpointsByUserIdAndSensorIdAndDate {}", sensorEndpoint);
                return null;
            }

            if(!currentSensorEndpoints.get(0).getValue().equals(sensorEndpoint.getValue())) {
                log.debug("Updated SensorEndpointDTO {}", sensorEndpoint);
                save(sensorEndpoint);
                return CompletableFuture.completedFuture(currentSensorEndpoints.get(0));
            }
        } else if(lastSensorEndpoint == null) {
            log.debug("New SensorEndpointDTO {}", sensorEndpoint);
            save(sensorEndpoint);
            return CompletableFuture.completedFuture(sensorEndpoint);
        }
        return null;
    }
}
