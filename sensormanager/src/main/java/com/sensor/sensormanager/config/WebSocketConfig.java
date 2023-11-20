package com.sensor.sensormanager.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

/**
 * https://medium.com/@parthiban.rajalingam/introduction-to-web-sockets-using-spring-boot-and-angular-b11e7363f051
 */
@Configuration
@EnableWebSocket
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketConfigurer {

    private final SensorValueWebSocketHandler sensorValueWebSocketHandler;

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(sensorValueWebSocketHandler,"/sensor-gui").setAllowedOrigins("*");
    }

}
