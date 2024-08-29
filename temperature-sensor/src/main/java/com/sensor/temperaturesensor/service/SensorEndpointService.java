package com.sensor.temperaturesensor.service;

import com.sensor.temperaturesensor.dto.SensorEndpointDTO;
import com.sensor.temperaturesensor.model.SensorEndpoint;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.concurrent.Future;

public interface SensorEndpointService {

    SensorEndpoint save(SensorEndpoint sensorEndpoint);
    Future<SensorEndpoint> recordSensorEndpoint(SensorEndpoint sensorEndpoint);
    List<SensorEndpointDTO> getByUserIdAndSensorIdAndDate(String userId, String sensorId,
                                                          OffsetDateTime fromDate, OffsetDateTime toDate, int pageNumber, int pageSize);
}
