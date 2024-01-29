package com.sensor.sensormanager.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sensor.sensormanager.dto.SensorEndpointDTO;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

@Component
@Slf4j
public class SensorValueWebSocketHandler extends TextWebSocketHandler {
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final List<WebSocketSession> sessions = Collections.synchronizedList(new ArrayList<>());

    @Override
    public void afterConnectionEstablished(@NotNull WebSocketSession session) throws Exception {

        super.afterConnectionEstablished(session);

        Date now = new Date();

        //Creating a SensorEndpointDTO Object
        SensorEndpointDTO sensorEndpointDTO = new SensorEndpointDTO("hello", "dummySensor", (float) now.getTime(),now);

        //Sending SensorEndpointDTO
        TextMessage message = new TextMessage(objectMapper.writeValueAsString(sensorEndpointDTO));
        session.sendMessage(message);

        this.sessions.add(session);

        log.debug("WebSocketSession count {}", sessions.size());
    }

    @Override
    public void afterConnectionClosed(@NotNull WebSocketSession session, @NotNull CloseStatus status) throws Exception {
        super.afterConnectionClosed(session, status);
        sessions.remove(session);
    }
    @Override
    public void handleMessage(@NotNull WebSocketSession session, @NotNull WebSocketMessage<?> message) throws Exception {

        super.handleMessage(session, message);
        for (WebSocketSession webSocketSession : sessions) {
            webSocketSession.sendMessage(message);
        }
    }

    public void sendMessage(final SensorEndpointDTO sensorEndpointDTO) throws IOException {

        WebSocketSession session;
        if (this.sessions.isEmpty()) {
            log.warn("No websocket sessions available");
            return;
        } else {
            session = this.sessions.get(0);
        }

        TextMessage message = new TextMessage(objectMapper.writeValueAsString(sensorEndpointDTO));
        session.sendMessage(message);

    }

    public boolean supportsPartialMessages() {
        return true;
    }
}
