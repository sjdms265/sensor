package com.sensor.temperaturesensor.service.impl;

import com.sensor.temperaturesensor.dto.SensorEndpointDTO;
import com.sensor.temperaturesensor.model.SensorEndpoint;
import com.sensor.temperaturesensor.repository.SensorEndpointRepository;
import com.sensor.temperaturesensor.service.SensorEndpointService;
import io.micrometer.observation.annotation.Observed;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.domain.Limit;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.ScrollPosition;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Window;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
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

    private final Converter<SensorEndpoint, SensorEndpointDTO> sensorEndpoint2SensorEnpointDTOConverter;

    @Value("${sensor-manager.pageSize:500}")
    private int pageSize;

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
                    getByUserIdAndSensorIdAndDate(sensorEndpoint.getUserId(), sensorEndpoint.getSensorId(),
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

    @Override
    public List<SensorEndpointDTO> getByUserIdAndSensorIdAndDate(String userId, String sensorId,
                                                                 OffsetDateTime fromDate, OffsetDateTime toDate, Integer pageNumber, Integer pageSize) {

        if(pageNumber == null || pageSize == null) {
            pageNumber = 0;
            pageSize = this.pageSize;
        }
        Pageable pageable = PageRequest.of(pageNumber, pageSize);

        List<SensorEndpoint> sensorEndpoints;
        if(fromDate == null && toDate == null) {
            sensorEndpoints = sensorEndpointRepository.getByUserIdAndSensorIdOrderByDateDesc(userId, sensorId, pageable);
        } else {
            assert fromDate != null;
            sensorEndpoints = sensorEndpointRepository.getByUserIdAndSensorIdAndDateBetweenOrderByDateDesc(userId, sensorId,
                    Date.from(fromDate.toInstant()),  Date.from(toDate.toInstant()), pageable);
        }

        return sensorEndpoints.stream().
                map(sensorEndpoint2SensorEnpointDTOConverter::convert).toList();
    }

    @Override
    public Window<SensorEndpoint> findByUserIdAndSensorId(String userId, String sensorId,
                                                          ScrollPosition scrollPosition, Limit limit, Sort sort) {
        return sensorEndpointRepository.findByUserIdAndSensorIdOrderByDateDesc(userId, sensorId, scrollPosition, limit, sort);

        /*FIXME return Window.from(sensorEndpoints.stream().
                map(sensorEndpoint2SensorEnpointDTOConverter::convert).toList(), (sp) -> scrollPosition);*/
    }
}
