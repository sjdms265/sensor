package com.sensor.temperaturesensor.service;

import com.sensor.sensormanager.dto.SensorEndpointDTO;
import com.sensor.temperaturesensor.model.SensorEndpoint;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

//@Component
@Slf4j
@RequiredArgsConstructor
public class KafkaListeners {

    private final SensorEndpointService sensorEndpointService;

//    @KafkaListener(topics = "${sensormanager.topic.temperaturechange}", containerFactory = "sensorEndpointFactory", groupId = "json")
    void listener(SensorEndpointDTO sensorEndpointDTO) {
        log.info("Listener received SensorEndpointDTO {}", sensorEndpointDTO);

        SensorEndpoint sensorEndpoint = SensorEndpoint.builder().userId(sensorEndpointDTO.getUserId()).
                sensorId(sensorEndpointDTO.getSensorId()).value(sensorEndpointDTO.getValue()).date(sensorEndpointDTO.getDate()).build();

        sensorEndpointService.recordSensorEndpoint(sensorEndpoint);

    }
}
