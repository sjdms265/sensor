package com.sensor.temperaturesensor.repository;

import com.sensor.temperaturesensor.model.SensorEndpoint;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Date;
import java.util.List;

public interface SensorEndpointRepository extends JpaRepository<SensorEndpoint, Long> {

    List<SensorEndpoint> getSensorEndpointsByUserIdAndSensorIdAndDate(String userId, String sensorId, Date date);

    List<SensorEndpoint> getByUserIdAndSensorIdAndDateBetween(String userId, String sensorId, Date fromDate, Date toDate);

    @Query(value = "SELECT max(date) FROM SensorEndpoint where userId = :userId and sensorId = :sensorId")
    Date getLastSavedSensorEndpoint(@Param("userId") String userId, @Param("sensorId") String sensorId);

    List<SensorEndpoint> getByUserIdAndSensorIdOrderByDateDesc(String userId, String sensorId);
}
