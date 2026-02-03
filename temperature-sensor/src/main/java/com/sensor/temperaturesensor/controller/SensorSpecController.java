package com.sensor.temperaturesensor.controller;


import com.sensor.sensorcommon.dto.SensorSpecDTO;
import com.sensor.temperaturesensor.service.SensorSpecService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping
@Slf4j
public class SensorSpecController {

    public static final String SENSOR_SPECS_PATH = "/sensorSpecs";

    private final SensorSpecService sensorSpecService;

    @PostMapping(SENSOR_SPECS_PATH)
    public ResponseEntity<SensorSpecDTO> saveSensorSpec(@RequestBody SensorSpecDTO sensorSpec) {
        log.debug("saveSensorSpec {}", sensorSpec);
        return new ResponseEntity<>(sensorSpecService.save(sensorSpec), HttpStatus.CREATED);
    }

    @GetMapping(SENSOR_SPECS_PATH)
    public ResponseEntity<List<SensorSpecDTO>> getSensorSpecs() {
        return ResponseEntity.ok(sensorSpecService.getSensorSpecs());
    }

    @GetMapping(SENSOR_SPECS_PATH + "/{id}")
    public ResponseEntity<SensorSpecDTO> getSensorSpec(@PathVariable String id) {
        return ResponseEntity.ok(sensorSpecService.getBySensorSpecById(id));
    }

    @DeleteMapping(SENSOR_SPECS_PATH + "/{id}")
    public ResponseEntity<SensorSpecDTO> deleteSensorSpec(@PathVariable String id) {
        SensorSpecDTO sensorSpec = sensorSpecService.getBySensorSpecById(id);
        return ResponseEntity.ok(sensorSpecService.delete(sensorSpec));
    }
}
