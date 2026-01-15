package com.sensor.sensormcpserver.service;

import com.sensor.sensormcpserver.dto.SensorEndpointDTO;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class SensorService {

    public List<SensorEndpointDTO> findByPattern(String pattern) {
        return new ArrayList<>();
    }
}
