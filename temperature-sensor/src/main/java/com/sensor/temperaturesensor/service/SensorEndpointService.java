package com.sensor.temperaturesensor.service;

import com.sensor.temperaturesensor.dto.SensorEndpointDTO;
import com.sensor.temperaturesensor.model.SensorEndpoint;
import org.springframework.data.domain.Limit;
import org.springframework.data.domain.ScrollPosition;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Window;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.concurrent.Future;

public interface SensorEndpointService {

    SensorEndpoint save(SensorEndpoint sensorEndpoint);
    Future<SensorEndpoint> recordSensorEndpoint(SensorEndpoint sensorEndpoint);
    List<SensorEndpointDTO> getByUserIdAndSensorIdAndDate(String userId, String sensorId,
                                                          OffsetDateTime fromDate, OffsetDateTime toDate, Integer pageNumber, Integer pageSize);

    Window<SensorEndpoint> findByUserIdAndSensorId(String userId, String sensorId, ScrollPosition scrollPosition, Limit limit, Sort sort);
}
