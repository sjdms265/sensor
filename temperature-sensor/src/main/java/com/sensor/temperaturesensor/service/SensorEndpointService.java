package com.sensor.temperaturesensor.service;

import com.sensor.temperaturesensor.model.SensorEndpoint;

import java.util.concurrent.Future;

public interface SensorEndpointService {

    SensorEndpoint save(SensorEndpoint sensorEndpoint);
    Future<SensorEndpoint> recordSensorEndpoint(SensorEndpoint sensorEndpoint);
}
