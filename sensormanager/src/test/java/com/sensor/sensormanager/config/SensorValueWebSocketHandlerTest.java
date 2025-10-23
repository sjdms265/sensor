package com.sensor.sensormanager.config;

import com.sensor.sensormanager.dto.SensorEndpointDTO;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.util.Date;

class SensorValueWebSocketHandlerTest {

    @Mock
    private WebSocketSession session;

    private SensorValueWebSocketHandler sensorValueWebSocketHandler;

    @BeforeEach
    void setUp() throws Exception {

        try (final AutoCloseable autoCloseable = MockitoAnnotations.openMocks(this)) {
            sensorValueWebSocketHandler = new SensorValueWebSocketHandler();
        }

    }

    @Test
    void afterConnectionEstablished() throws Exception {

        Mockito.doNothing().when(session).sendMessage(Mockito.any());

        sensorValueWebSocketHandler.afterConnectionEstablished(session);

        Assertions.assertFalse(sensorValueWebSocketHandler.getSessions().isEmpty());
    }

    @Test
    void afterConnectionClosed() throws Exception {

        sensorValueWebSocketHandler.afterConnectionEstablished(session);

        Assertions.assertFalse(sensorValueWebSocketHandler.getSessions().isEmpty());

        CloseStatus status = Mockito.mock(CloseStatus.class);
        sensorValueWebSocketHandler.afterConnectionClosed(session, status);

        Assertions.assertTrue(sensorValueWebSocketHandler.getSessions().isEmpty());
    }

    @Test
    void sendMessage() throws Exception {

        sensorValueWebSocketHandler.afterConnectionEstablished(session);

        Assertions.assertFalse(sensorValueWebSocketHandler.getSessions().isEmpty());

        Date now = new Date();
        SensorEndpointDTO sensorEndpointDTO = new SensorEndpointDTO("hello", "dummySensor", (float) now.getTime(),now);
        TextMessage textMessage = sensorValueWebSocketHandler.sendMessage(sensorEndpointDTO);

        Assertions.assertNotNull(textMessage);
    }

    @Test
    void sendMessageNoSession() throws Exception {

        Date now = new Date();
        SensorEndpointDTO sensorEndpointDTO = new SensorEndpointDTO("hello", "dummySensor", (float) now.getTime(),now);
        TextMessage textMessage = sensorValueWebSocketHandler.sendMessage(sensorEndpointDTO);

        Assertions.assertNull(textMessage);
    }

}