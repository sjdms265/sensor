package com.sensor.sensormanager.config;

import lombok.RequiredArgsConstructor;
import org.apache.camel.Processor;
import org.apache.camel.builder.RouteBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class Mqtt2Kafka extends RouteBuilder {

    @Value(value = "${sensor-manager.topic.sensor-value}")
    private String topic;

    private final Processor sensorEndpointKeyProcessor;

    /**
     * <a href="https://camel.apache.org/manual/route-configuration.html">...</a>
     * <a href="https://stackoverflow.com/questions/35042672/read-mqtt-topic-of-received-message-in-camel-route">...</a>
     * <a href="https://camel.apache.org/components/3.20.x/kafka-component.html">...</a>
     */
    @Override
    public void configure() {

        //fixme YAML DSL
        from("paho-mqtt5:temperature").routeId("consumeTemperature")
                .process(sensorEndpointKeyProcessor)
                .log("Message read from topic ${in.header.CamelMQTTSubscribeTopic} body ${body} key ${in.header.kafka.KEY}.")
//                .toD("kafka:" + topic + "?key=${in.header.kafka.KEY}");
                .to("kafka:" + topic);

        from("paho-mqtt5:humidity").routeId("consumeHumidity")
                .process(sensorEndpointKeyProcessor)
                .log("Message read from topic ${in.header.CamelMQTTSubscribeTopic} body ${body} key ${in.header.kafka.KEY}.")
                .toD("kafka:" + topic);
    }
}
