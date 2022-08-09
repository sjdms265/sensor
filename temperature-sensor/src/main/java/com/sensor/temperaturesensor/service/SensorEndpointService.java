package com.sensor.temperaturesensor.service;

import com.sensor.temperaturesensor.model.SensorEndpoint;

public interface SensorEndpointService {

    SensorEndpoint save(SensorEndpoint sensorEndpoint);
    SensorEndpoint recordSensorEndpoint(SensorEndpoint sensorEndpoint);
}
