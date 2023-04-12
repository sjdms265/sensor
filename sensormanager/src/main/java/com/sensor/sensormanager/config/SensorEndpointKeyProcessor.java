package com.sensor.sensormanager.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sensor.sensormanager.dto.SensorEndpointDTO;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.component.kafka.KafkaConstants;
import org.springframework.stereotype.Component;

@Component
public class SensorEndpointKeyProcessor implements Processor {
    @Override
    public void process(Exchange exchange) throws Exception {
        String body = exchange.getIn().getBody(String.class);

        SensorEndpointDTO sensorEndpointDTO = new ObjectMapper().readValue(body, SensorEndpointDTO.class);
        exchange.getIn().setHeader(KafkaConstants.KEY, sensorEndpointDTO.getUserId());
    }
}
