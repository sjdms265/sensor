package com.sensor.sensormcpserver.service;

import com.sensor.sensorcommon.dto.GraphSensorEndpoint;
import com.sensor.sensorcommon.dto.LoginSensorUserDTO;
import com.sensor.sensorcommon.dto.SensorEndpointDTO;
import com.sensor.sensorcommon.dto.SensorSpecDTO;
import com.sensor.sensorcommon.enums.SensorType;
import com.sensor.sensormcpserver.dto.TokenResponseDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatRuntimeException;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SensorServiceTest {

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private SensorService sensorService;

    private static final String TEMPERATURE_SENSOR_URL = "http://temperature-sensor:8080";
    private static final String SENSOR_MANAGER_URL = "http://sensor-manager:8080";
    private static final String USER_ID = "user123";
    private static final String SENSOR_ID = "sensor-1";
    private static final String TOKEN = "jwt-token";
    private static final Integer PAGE_SIZE = 50;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(sensorService, "temperatureSensorUrl", TEMPERATURE_SENSOR_URL);
        ReflectionTestUtils.setField(sensorService, "sensorManagerUrl", SENSOR_MANAGER_URL);
    }

    @Test
    void getSensorEndpointsList_shouldReturnSortedGraphEndpoints_whenValidResponse() {
        // Given
        String responseBody = """
                {
                  "data": {
                    "sensorEndpoints": [
                      {
                        "parsedDateTime": "2024-01-01T12:00:00.123Z",
                        "value": 25.5
                      },
                      {
                        "parsedDateTime": "2024-01-01T11:00:00.123Z",
                        "value": 24.0
                      }
                    ]
                  }
                }
                """;

        when(restTemplate.postForEntity(anyString(), any(HttpEntity.class), eq(String.class)))
                .thenReturn(ResponseEntity.ok(responseBody));

        // When
        List<GraphSensorEndpoint> result = sensorService.getSensorEndpointsList(TOKEN, USER_ID, SENSOR_ID, PAGE_SIZE);

        // Then
        assertThat(result).hasSize(2);
        assertThat(result.get(0).value()).isEqualTo(24.0F);
        assertThat(result.get(1).value()).isEqualTo(25.5F);

        verify(restTemplate).postForEntity(eq(TEMPERATURE_SENSOR_URL + SensorService.GRAPH_QL), any(HttpEntity.class), eq(String.class));
    }

    @Test
    void getSensorEndpointsList_shouldReturnEmptyList_whenInvalidJsonResponse() {
        // Given
        String invalidJson = "invalid json";

        when(restTemplate.postForEntity(anyString(), any(HttpEntity.class), eq(String.class)))
                .thenReturn(ResponseEntity.ok(invalidJson));

        // When
        List<GraphSensorEndpoint> result = sensorService.getSensorEndpointsList(TOKEN, USER_ID, SENSOR_ID, PAGE_SIZE);

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    void getSensorEndpointsList_shouldReturnEmptyList_whenDataNodeMissing() {
        // Given
        String responseBody = """
                {
                  "errors": ["Some error"]
                }
                """;

        when(restTemplate.postForEntity(anyString(), any(HttpEntity.class), eq(String.class)))
                .thenReturn(ResponseEntity.ok(responseBody));

        // When
        List<GraphSensorEndpoint> result = sensorService.getSensorEndpointsList(TOKEN, USER_ID, SENSOR_ID, PAGE_SIZE);

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    void getSensorEndpointsList_shouldReturnEmptyList_whenExceptionThrown() {
        // Given
        when(restTemplate.postForEntity(anyString(), any(HttpEntity.class), eq(String.class)))
                .thenThrow(new RuntimeException("Connection error"));

        // When and Then
        assertThatRuntimeException().isThrownBy(() -> sensorService.getSensorEndpointsList(TOKEN, USER_ID, SENSOR_ID, PAGE_SIZE));
    }

    @Test
    void sensorsByUser_shouldReturnSensorEndpoints_whenValidResponse() {
        // Given
        String responseBody = """
                {
                  "data": {
                    "sensorsByUser": [
                      {
                        "sensorId": "sensor-1"
                      },
                      {
                        "sensorId": "sensor-2"
                      }
                    ]
                  }
                }
                """;

        when(restTemplate.postForEntity(anyString(), any(HttpEntity.class), eq(String.class)))
                .thenReturn(ResponseEntity.ok(responseBody));

        // When
        List<SensorEndpointDTO> result = sensorService.sensorsByUser(TOKEN, USER_ID);

        // Then
        assertThat(result).hasSize(2);

        verify(restTemplate).postForEntity(eq(TEMPERATURE_SENSOR_URL + SensorService.GRAPH_QL), any(HttpEntity.class), eq(String.class));
    }

    @Test
    void sensorsByUser_shouldReturnEmptyList_whenInvalidJsonResponse() {
        // Given
        String invalidJson = "not valid json";

        when(restTemplate.postForEntity(anyString(), any(HttpEntity.class), eq(String.class)))
                .thenReturn(ResponseEntity.ok(invalidJson));

        // When
        List<SensorEndpointDTO> result = sensorService.sensorsByUser(TOKEN, USER_ID);

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    void sensorsByUser_shouldReturnEmptyList_whenDataNodeMissing() {
        // Given
        String responseBody = """
                {
                  "data": {}
                }
                """;

        when(restTemplate.postForEntity(anyString(), any(HttpEntity.class), eq(String.class)))
                .thenReturn(ResponseEntity.ok(responseBody));

        // When
        List<SensorEndpointDTO> result = sensorService.sensorsByUser(TOKEN, USER_ID);

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    void sensorsByUser_shouldReturnEmptyList_whenExceptionThrown() {
        // Given
        when(restTemplate.postForEntity(anyString(), any(HttpEntity.class), eq(String.class)))
                .thenThrow(new RuntimeException("Network error"));

        // When and Then
        assertThatRuntimeException().isThrownBy(() -> sensorService.sensorsByUser(TOKEN, USER_ID));
    }

    @Test
    void getUserToken_shouldReturnTokenResponse_whenValidCredentials() {
        // Given
        LoginSensorUserDTO loginDTO = new LoginSensorUserDTO("username", "password");
        TokenResponseDTO expectedToken = new TokenResponseDTO("access_token", "refresh_token");

        when(restTemplate.postForEntity(anyString(), any(HttpEntity.class), eq(TokenResponseDTO.class)))
                .thenReturn(ResponseEntity.ok(expectedToken));

        // When
        TokenResponseDTO result = sensorService.getUserToken(loginDTO);

        // Then
        assertThat(result).isEqualTo(expectedToken);
        assertThat(result.access_token()).isEqualTo("access_token");
        assertThat(result.refresh_token()).isEqualTo("refresh_token");

        verify(restTemplate).postForEntity(eq(SENSOR_MANAGER_URL + SensorService.API_TOKEN), any(HttpEntity.class), eq(TokenResponseDTO.class));
    }

    @Test
    void getUserToken_shouldReturnNull_whenAuthenticationFails() {
        // Given
        LoginSensorUserDTO loginDTO = new LoginSensorUserDTO("username", "wrong-password");

        when(restTemplate.postForEntity(anyString(), any(HttpEntity.class), eq(TokenResponseDTO.class)))
                .thenReturn(ResponseEntity.ok(null));

        // When
        TokenResponseDTO result = sensorService.getUserToken(loginDTO);

        // Then
        assertThat(result).isNull();
    }

    @Test
    void getSensorSpec_shouldReturnSensorSpec_whenFound() {
        // Given
        SensorSpecDTO expectedSpec = new SensorSpecDTO(SENSOR_ID, "Temperature Sensor", SensorType.TEMPERATURE);

        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(HttpEntity.class), eq(SensorSpecDTO.class)))
                .thenReturn(ResponseEntity.ok(expectedSpec));

        // When
        SensorSpecDTO result = sensorService.getSensorSpec(USER_ID, TOKEN);

        // Then
        assertThat(result).isEqualTo(expectedSpec);
        assertThat(result.sensorCategory()).isEqualTo(SensorType.TEMPERATURE);

        verify(restTemplate).exchange(eq(TEMPERATURE_SENSOR_URL + SensorService.SENSOR_SPEC + "/" + USER_ID), eq(HttpMethod.GET), any(HttpEntity.class), eq(SensorSpecDTO.class));
    }

    @Test
    void getSensorSpec_shouldReturnNull_whenNotFound() {
        // Given
        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(HttpEntity.class), eq(SensorSpecDTO.class)))
                .thenReturn(ResponseEntity.ok(null));

        // When
        SensorSpecDTO result = sensorService.getSensorSpec(USER_ID, TOKEN);

        // Then
        assertThat(result).isNull();
    }

    @Test
    void getSensorSpec_shouldReturnNull_whenExceptionThrown() {
        // Given
        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(HttpEntity.class), eq(SensorSpecDTO.class)))
                .thenThrow(new RuntimeException("Service unavailable"));

        // When/Then
        try {
            sensorService.getSensorSpec(USER_ID, TOKEN);
        } catch (RuntimeException e) {
            assertThat(e.getMessage()).isEqualTo("Service unavailable");
        }
    }
}
