package com.sensor.temperaturesensor.repository;

import com.sensor.temperaturesensor.model.SensorSpec;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SensorSpecRepository extends JpaRepository<SensorSpec, Long> {

    SensorSpec findSensorSpecById(String id);
}
