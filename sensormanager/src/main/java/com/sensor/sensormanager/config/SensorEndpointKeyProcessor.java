package com.sensor.sensormanager.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sensor.sensormanager.dto.SensorEndpointDTO;
import com.sensor.sensormanager.model.SensorUser;
import com.sensor.sensormanager.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.component.kafka.KafkaConstants;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class SensorEndpointKeyProcessor implements Processor {

    private final ObjectMapper objectMapper;

    private final UserService userService;

    @Override
    public void process(Exchange exchange) throws Exception {

        String body = exchange.getIn().getBody(String.class);
        String topic  = (String) exchange.getIn().getHeader("CamelMqttTopic");
        log.debug("SensorEndpoint body {} topic {}", body, topic);

        SensorEndpointDTO sensorEndpointDTO = objectMapper.readValue(body, SensorEndpointDTO.class);
        String userId = sensorEndpointDTO.getUserId();
        SensorUser sensorUser = userService.getByUsername(userId);

        if(sensorUser != null) {
            log.debug("sensorUser {}", sensorUser);
            exchange.getIn().setHeader(KafkaConstants.KEY, sensorEndpointDTO.getUserId() + "-" + sensorEndpointDTO.getSensorId());
        } else {
            log.error("userId {} doesn't exist", userId);
        }

    }
}
