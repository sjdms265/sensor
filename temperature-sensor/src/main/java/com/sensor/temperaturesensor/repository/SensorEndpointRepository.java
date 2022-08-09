package com.sensor.temperaturesensor.repository;

import com.sensor.temperaturesensor.model.SensorEndpoint;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Date;

public interface SensorEndpointRepository extends JpaRepository<SensorEndpoint, Long> {

    SensorEndpoint getSensorEndpointByUserIdAndSensorIdAndDate(String userId, String sensorId, Date date);

    @Query(value = "SELECT max(date) FROM SensorEndpoint where userId = :userId and sensorId = :sensorId")
    Date getLastSavedSensorEndpoint(@Param("userId") String userId, @Param("sensorId") String sensorId);

}
