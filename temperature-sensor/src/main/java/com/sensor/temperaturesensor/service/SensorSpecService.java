package com.sensor.temperaturesensor.service;

import com.sensor.sensorcommon.dto.SensorSpecDTO;

import java.util.List;

public interface SensorSpecService {

    SensorSpecDTO save(SensorSpecDTO sensorSpecDTO);

    List<SensorSpecDTO> getSensorSpecs();

    SensorSpecDTO getBySensorSpecById(String id);

    SensorSpecDTO delete(SensorSpecDTO sensorSpecDTO);

}
