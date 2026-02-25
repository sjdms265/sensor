package com.sensor.sensormanager.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sensor.sensorcommon.dto.SensorEndpointDTO;
import com.sensor.sensormanager.model.SensorUser;
import com.sensor.sensormanager.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;

import java.util.function.Function;

/**
 * Spring Cloud Stream integration for MQTT to Kafka bridge.
 * Replaces Apache Camel-based Mqtt2Kafka implementation.
 *
 * This configuration defines a function that:
 * 1. Receives messages from MQTT (via Spring Cloud Stream MQTT source)
 * 2. Processes the message (validates user, sets Kafka key)
 * 3. Sends to WebSocket clients
 * 4. Forwards to Kafka topic
 */
@Configuration
@RequiredArgsConstructor
@Slf4j
public class SpringCloudMqtt2Kafka {

    private final ObjectMapper objectMapper;
    private final UserService userService;
    private final SensorValueWebSocketHandler sensorValueWebSocketHandler;

    /**
     * Spring Cloud Stream function that processes sensor values from MQTT to Kafka.
     * Function binding name: sensorValueProcessor-in-0 (input from MQTT)
     *                       sensorValueProcessor-out-0 (output to Kafka)
     */
    @Bean
    public Function<Message<String>, Message<SensorEndpointDTO>> sensorValueProcessor() {
        return message -> {
            try {
                String body = message.getPayload();
                String topic = message.getHeaders().get("mqtt_receivedTopic", String.class);

                log.debug("Message received from MQTT topic {} body {}", topic, body);

                // Parse the sensor data
                SensorEndpointDTO sensorEndpointDTO = objectMapper.readValue(body, SensorEndpointDTO.class);
                String userId = sensorEndpointDTO.getUserId();

                // Validate user exists
                SensorUser sensorUser = userService.getByUsername(userId);
                if (sensorUser == null) {
                    log.error("userId {} doesn't exist", userId);
                    return null; // Skip invalid messages
                }

                log.debug("Processing message for sensorUser {}", sensorUser);

                // Create Kafka key (similar to SensorEndpointKeyProcessor)
                String key = sensorEndpointDTO.getUserId() + "-" + sensorEndpointDTO.getSensorId();

                // Send to WebSocket clients
                try {
                    sensorValueWebSocketHandler.sendMessage(sensorEndpointDTO);
                } catch (Exception e) {
                    log.error("Error sending message to WebSocket", e);
                    // Continue processing even if WebSocket fails
                }

                // Build message for Kafka with key header
                Message<SensorEndpointDTO> kafkaMessage = MessageBuilder
                        .withPayload(sensorEndpointDTO)
                        .setHeader(KafkaHeaders.KEY, key)
                        .build();

                log.debug("Message processed successfully with key {}", key);
                return kafkaMessage;

            } catch (Exception e) {
                log.error("UNHANDLED EXCEPTION processing message: {}", e.getMessage(), e);
                handleFallback(message.getPayload(), e);
                return null; // Skip failed messages
            }
        };
    }

    private void handleFallback(String body, Exception originalException) {
        log.error("Critical: MQTT message processing failed. Message: {}, Error: {}",
                body, originalException.getMessage());
        // Implement fallback logic here (e.g., save to database, dead letter queue, etc.)
    }
}
