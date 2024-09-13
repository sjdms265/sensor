package com.sensor.sensormanager.service;

import com.sensor.sensormanager.model.Role;
import com.sensor.sensormanager.model.SensorUser;

import java.util.List;

public interface UserService {

    SensorUser saveUser(SensorUser sensorUser);

    Role saveRole(Role role);

    Role addRoleToUser(String username, String roleName);

    List<SensorUser> getUsers();

    SensorUser getByUsername(String username);

    SensorUser getByUsernameAndPassword(String username, String password);

    SensorUser deleteUser(String username);

    void deleteRole(String name);
}
