package com.sensor.sensormanager.dto;

import java.util.Collection;

/*
    https://stackoverflow.com/questions/62455515/how-can-i-use-java-records-as-dto-with-modelmapper
 */
public record SensorUserDTO(String name, String username, String password, Collection<String> roles) {

}