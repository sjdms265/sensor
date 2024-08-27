package com.sensor.sensormanager.config;

import com.sensor.sensormanager.model.SensorUser;
import com.sensor.sensormanager.service.UserService;
import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.component.kafka.KafkaConstants;
import org.apache.camel.impl.DefaultCamelContext;
import org.apache.camel.support.DefaultExchange;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.AutoConfigureJson;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

/*
 * https://medium.com/testing-spring-boot-java-applications-cookbook/testing-objectmapper-component-a3b615bb959f
 */
@SpringBootTest(classes = {
        SensorEndpointKeyProcessor.class
})
@AutoConfigureJson
class SensorEndpointKeyProcessorTest {

    @MockBean
    private UserService userService;

    @Autowired
    private SensorEndpointKeyProcessor processor;

    @Test
    void process() throws Exception {

        Exchange exchange = getExchange();

        Mockito.when(userService.getByUsername(Mockito.anyString())).thenReturn(new SensorUser());

        processor.process(exchange);

        Assertions.assertNotNull(exchange.getIn().getHeader(KafkaConstants.KEY, String.class));

    }

    @Test
    void processUserNull() throws Exception {

        Exchange exchange = getExchange();

        Mockito.when(userService.getByUsername(Mockito.anyString())).thenReturn(null);

        processor.process(exchange);

        Assertions.assertNull(exchange.getIn().getHeader(KafkaConstants.KEY, String.class));

    }

    @NotNull
    private static Exchange getExchange() {
        String payload = """
                {
                    "sensorId" : "sensorId",
                    "value" : 1.0,
                    "userId" : "userId"
                }
                """;

        //https://stackoverflow.com/questions/15113550/unit-testing-functions-with-apache-camel-exchange-as-parameter
        CamelContext ctx = new DefaultCamelContext();
        Exchange exchange = new DefaultExchange(ctx);
        exchange.getIn().setBody(payload);
        return exchange;
    }
}