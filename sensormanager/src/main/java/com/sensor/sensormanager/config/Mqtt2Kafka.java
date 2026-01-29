package com.sensor.sensormanager.config;

import lombok.RequiredArgsConstructor;
import org.apache.camel.LoggingLevel;
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

    private final SensorValueWebSocketHandler sensorValueWebSocketHandler;

    /**
     * <a href="https://camel.apache.org/manual/route-configuration.html">...</a>
     * <a href="https://stackoverflow.com/questions/35042672/read-mqtt-topic-of-received-message-in-camel-route">...</a>
     * <a href="https://camel.apache.org/components/3.20.x/kafka-component.html">...</a>
     */
    @Override
    public void configure() {

        /* -------------------------------------------------------------
         * 1️⃣ Global safety‑net – catches anything we forget.
         * ------------------------------------------------------------- */
        onException(Exception.class)
                .handled(true)                               // prevents context shutdown
                .log(LoggingLevel.ERROR,
                        "UNHANDLED EXCEPTION in ${routeId}: ${exception.stacktrace}");
//                .to("activemq:queue:genericDlq");           // or any DLQ you prefer

        /* -------------------------------------------------------------
         * 2️⃣ Specific handling for SocketTimeoutException that can be
         *    thrown by the Paho client when the broker does not answer
         *    within the configured keep‑alive / connection timeout.
         * ------------------------------------------------------------- */
        onException(java.net.SocketTimeoutException.class)
                .maximumRedeliveries(2)                     // optional retries
                .redeliveryDelay(1500)                      // 1.5s pause between tries
                .retryAttemptedLogLevel(LoggingLevel.WARN)
                .handled(true)                               // <‑‑ crucial
                .log(LoggingLevel.WARN,
                        "Socket timeout while consuming from Mosquitto – routing to fallback.")
                .to("direct:mqttConsumeFallback");

        //fixme YAML DSL
        from("paho-mqtt5:sensorValue")      // 20s MQTT keep‑alive
                .routeId("consumeSensorValue")
                .process(sensorEndpointKeyProcessor)
//                .log(LoggingLevel.INFO, "Message read from topic ${in.header.CamelMQTTSubscribeTopic} body ${body} key ${in.header.kafka.KEY}.")
                .toD("kafka:" + topic)
                .bean(sensorValueWebSocketHandler, "sendMessage(${body})");

        // 3. Fallback Route (No Circuit Breaker)
        from("direct:mqttConsumeFallback")
                .routeId("mqtt-fallback-route")
                .log("Critical: MQTT Broker unavailable after retries. Saving to fallback.");

    }
}
