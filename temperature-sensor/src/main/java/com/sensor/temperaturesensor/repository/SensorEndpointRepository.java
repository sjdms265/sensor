package com.sensor.temperaturesensor.repository;

import com.sensor.temperaturesensor.model.SensorEndpoint;
import org.springframework.data.domain.Limit;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.ScrollPosition;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Window;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Date;
import java.util.List;

public interface SensorEndpointRepository extends JpaRepository<SensorEndpoint, Long> {

    List<SensorEndpoint> getByUserIdAndSensorIdAndDate(String userId, String sensorId, Date date);

    @Query("SELECT AVG(value) FROM SensorEndpoint se where se.userId = :userId and se.sensorId = :sensorId and se.date between :fromDate AND :toDate")
    Float calculateAverageValueByUserIdAndSensorIdAndDateBetween(String userId, String sensorId, Date fromDate, Date toDate);

    List<SensorEndpoint> getByUserIdAndSensorIdAndValueLessThanAndDateBetweenOrderByDateDesc(String userId, String sensorId, Float value, Date fromDate, Date toDate, Pageable pageable);

    @Query(value = "SELECT max(date) FROM SensorEndpoint where userId = :userId and sensorId = :sensorId")
    Date getLastSavedSensorEndpoint(@Param("userId") String userId, @Param("sensorId") String sensorId);

    List<SensorEndpoint> getByUserIdAndSensorIdOrderByDateDesc(String userId, String sensorId, Pageable pageable);

    Window<SensorEndpoint> findByUserIdAndSensorIdOrderByDateDesc(String userId, String sensorId,
                                                                  ScrollPosition scrollPosition, Limit limit, Sort sortOrder);

    @Query("SELECT DISTINCT s.sensorId FROM SensorEndpoint s WHERE s.userId = :userId")
    List<String> getDistinctSensorIdByUserId(@Param("userId") String userId);
}
