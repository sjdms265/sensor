package com.sensor.sensormanager.repository;

import com.sensor.sensormanager.model.SensorUser;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository  extends JpaRepository<SensorUser, Long> {

    SensorUser getByUsername(String username);
}
