package com.sensor.sensormanager.config;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sensor.sensorcommon.dto.SensorEndpointDTO;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
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

    private final ObjectMapper objectMapper = new ObjectMapper()
            .setSerializationInclusion(JsonInclude.Include.NON_NULL);
    @Getter
    private final List<WebSocketSession> sessions = Collections.synchronizedList(new ArrayList<>());

    @Override
    public void afterConnectionEstablished(@NotNull WebSocketSession session) throws Exception {

        super.afterConnectionEstablished(session);

        Date now = new Date();

        //Creating a SensorEndpointDTO Object
        SensorEndpointDTO sensorEndpointDTO = new SensorEndpointDTO("hello " + session.getId(), "dummySensor",
                (float) now.getTime(), now, null);

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

    /*
     * Called by camel route
     */
    public TextMessage sendMessage(final SensorEndpointDTO sensorEndpointDTO) throws Exception {

        TextMessage message;
        if (this.sessions.isEmpty()) {
            log.warn("No websocket sessions available");
            return null;
        } else {
            message = new TextMessage(objectMapper.writeValueAsString(sensorEndpointDTO));
            WebSocketSession session = this.sessions.getFirst();
            super.handleMessage(session, message);
            try {
                if(!CollectionUtils.isEmpty(this.sessions))
                    this.sessions.forEach(webSocketSession -> {
                        try {
                            webSocketSession.sendMessage(message);
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    });
            } catch (IllegalStateException ise) {
                log.error("Error sending message {} error {}", message, ise.getMessage());
            }

        }
        return message;
    }

    @Override
    public boolean supportsPartialMessages() {
        return true;
    }

}
