package com.sensor.sensormanager.service;

import com.sensor.sensormanager.model.Role;
import com.sensor.sensormanager.model.SensorUser;

import java.util.List;

public interface UserService {

    SensorUser saveUser(SensorUser sensorUser);

    Role saveRole(Role role);

    void addRoleToUser(String username, String roleName);

    SensorUser getUser(String username);

    List<SensorUser> getUsers();
}