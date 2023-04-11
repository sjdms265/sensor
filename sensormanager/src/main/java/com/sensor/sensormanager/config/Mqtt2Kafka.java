package com.sensor.sensormanager.config;

import org.apache.camel.builder.RouteBuilder;
import org.springframework.stereotype.Component;

@Component
public class Mqtt2Kafka extends RouteBuilder {

    /**
     * https://camel.apache.org/manual/route-configuration.html
     * https://stackoverflow.com/questions/35042672/read-mqtt-topic-of-received-message-in-camel-route
     * https://camel.apache.org/components/3.20.x/kafka-component.html
     * @throws Exception
     */
    @Override
    public void configure() throws Exception {

        //fixme YAML DSL
        from("paho-mqtt5:temperature?brokerUrl=tcp://192.168.50.4:1883").routeId("consumeTemperature")
                .log("Message read from topic ${in.header.CamelMQTTSubscribeTopic} body ${body}.")
                .to("kafka:temperature?brokers=localhost:9092&key=sjdms265");

        from("paho-mqtt5:humidity?brokerUrl=tcp://192.168.50.4:1883").routeId("consumeHumidity")
                .log("Message read from topic ${in.header.CamelMQTTSubscribeTopic} body ${body}.")
                .to("kafka:temperature?brokers=localhost:9092&key=sjdms265");
    }
}
