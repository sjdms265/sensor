package com.sensor.sensormcpserver.service;

import com.fasterxml.jackson.databind.ObjectMapper;
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
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatRuntimeException;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings({"rawtypes", "unchecked"})
class SensorServiceTest {

    @Mock
    private WebClient webClient;
    @Mock
    private WebClient.RequestBodyUriSpec requestBodyUriSpec;
    @Mock
    private WebClient.RequestBodySpec requestBodySpec;
    @Mock
    @SuppressWarnings("rawtypes")
    private WebClient.RequestHeadersUriSpec requestHeadersUriSpec;
    @Mock
    @SuppressWarnings("rawtypes")
    private WebClient.RequestHeadersSpec requestHeadersSpec;
    @Mock
    private WebClient.ResponseSpec responseSpec;
    @Spy
    private ObjectMapper objectMapper = new ObjectMapper();

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

    @SuppressWarnings("unchecked")
    private void stubPostWithAuthChain(Mono<?> responseMono) {
        when(webClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri(anyString())).thenReturn(requestBodySpec);
        when(requestBodySpec.contentType(any(MediaType.class))).thenReturn(requestBodySpec);
        when(requestBodySpec.header(anyString(), anyString())).thenReturn(requestBodySpec);
        when(requestBodySpec.bodyValue(any())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(any(Class.class))).thenReturn(responseMono);
    }

    @Test
    void getSensorEndpointsList_shouldReturnSortedGraphEndpoints_whenValidResponse() {
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

        stubPostWithAuthChain(Mono.just(responseBody));

        List<GraphSensorEndpoint> result = sensorService.getSensorEndpointsList(TOKEN, USER_ID, SENSOR_ID, PAGE_SIZE);

        assertThat(result).hasSize(2);
        assertThat(result.get(0).value()).isEqualTo(24.0F);
        assertThat(result.get(1).value()).isEqualTo(25.5F);
        verify(requestBodyUriSpec).uri(TEMPERATURE_SENSOR_URL + SensorService.GRAPH_QL);
    }

    @Test
    void getSensorEndpointsList_shouldReturnEmptyList_whenInvalidJsonResponse() {
        stubPostWithAuthChain(Mono.just("invalid json"));

        List<GraphSensorEndpoint> result = sensorService.getSensorEndpointsList(TOKEN, USER_ID, SENSOR_ID, PAGE_SIZE);

        assertThat(result).isEmpty();
    }

    @Test
    void getSensorEndpointsList_shouldReturnEmptyList_whenDataNodeMissing() {
        stubPostWithAuthChain(Mono.just("""
                {
                  "errors": ["Some error"]
                }
                """));

        List<GraphSensorEndpoint> result = sensorService.getSensorEndpointsList(TOKEN, USER_ID, SENSOR_ID, PAGE_SIZE);

        assertThat(result).isEmpty();
    }

    @Test
    void getSensorEndpointsList_shouldPropagateException_whenWebClientThrows() {
        when(webClient.post()).thenThrow(new RuntimeException("Connection error"));

        assertThatRuntimeException().isThrownBy(() -> sensorService.getSensorEndpointsList(TOKEN, USER_ID, SENSOR_ID, PAGE_SIZE));
    }

    @Test
    void sensorsByUser_shouldReturnSensorEndpoints_whenValidResponse() {
        stubPostWithAuthChain(Mono.just("""
                {
                  "data": {
                    "sensorsByUser": [
                      { "sensorId": "sensor-1" },
                      { "sensorId": "sensor-2" }
                    ]
                  }
                }
                """));

        List<SensorEndpointDTO> result = sensorService.sensorsByUser(TOKEN, USER_ID);

        assertThat(result).hasSize(2);
        verify(requestBodyUriSpec).uri(TEMPERATURE_SENSOR_URL + SensorService.GRAPH_QL);
    }

    @Test
    void sensorsByUser_shouldReturnEmptyList_whenInvalidJsonResponse() {
        stubPostWithAuthChain(Mono.just("not valid json"));

        List<SensorEndpointDTO> result = sensorService.sensorsByUser(TOKEN, USER_ID);

        assertThat(result).isEmpty();
    }

    @Test
    void sensorsByUser_shouldReturnEmptyList_whenDataNodeMissing() {
        stubPostWithAuthChain(Mono.just("""
                {
                  "data": {}
                }
                """));

        List<SensorEndpointDTO> result = sensorService.sensorsByUser(TOKEN, USER_ID);

        assertThat(result).isEmpty();
    }

    @Test
    void sensorsByUser_shouldPropagateException_whenWebClientThrows() {
        when(webClient.post()).thenThrow(new RuntimeException("Network error"));

        assertThatRuntimeException().isThrownBy(() -> sensorService.sensorsByUser(TOKEN, USER_ID));
    }

    @Test
    void getUserToken_shouldReturnTokenResponse_whenValidCredentials() {
        LoginSensorUserDTO loginDTO = new LoginSensorUserDTO("username", "password");
        TokenResponseDTO expectedToken = new TokenResponseDTO("access_token", "refresh_token");

        when(webClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri(anyString())).thenReturn(requestBodySpec);
        when(requestBodySpec.contentType(any(MediaType.class))).thenReturn(requestBodySpec);
        when(requestBodySpec.bodyValue(any())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(TokenResponseDTO.class)).thenReturn(Mono.just(expectedToken));

        TokenResponseDTO result = sensorService.getUserToken(loginDTO);

        assertThat(result).isEqualTo(expectedToken);
        assertThat(result.access_token()).isEqualTo("access_token");
        assertThat(result.refresh_token()).isEqualTo("refresh_token");
        verify(requestBodyUriSpec).uri(SENSOR_MANAGER_URL + SensorService.API_TOKEN);
    }

    @Test
    void getUserToken_shouldReturnNull_whenEmptyResponse() {
        LoginSensorUserDTO loginDTO = new LoginSensorUserDTO("username", "wrong-password");

        when(webClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri(anyString())).thenReturn(requestBodySpec);
        when(requestBodySpec.contentType(any(MediaType.class))).thenReturn(requestBodySpec);
        when(requestBodySpec.bodyValue(any())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(TokenResponseDTO.class)).thenReturn(Mono.empty());

        TokenResponseDTO result = sensorService.getUserToken(loginDTO);

        assertThat(result).isNull();
    }

    @Test
    void getSensorSpec_shouldReturnSensorSpec_whenFound() {
        SensorSpecDTO expectedSpec = new SensorSpecDTO(SENSOR_ID, "Temperature Sensor", SensorType.TEMPERATURE);

        when(webClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(anyString())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.header(anyString(), anyString())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(SensorSpecDTO.class)).thenReturn(Mono.just(expectedSpec));

        SensorSpecDTO result = sensorService.getSensorSpec(USER_ID, TOKEN);

        assertThat(result).isEqualTo(expectedSpec);
        assertThat(result.sensorCategory()).isEqualTo(SensorType.TEMPERATURE);
        verify(requestHeadersUriSpec).uri(TEMPERATURE_SENSOR_URL + SensorService.SENSOR_SPEC + "/" + USER_ID);
    }

    @Test
    void getSensorSpec_shouldReturnNull_whenNotFound() {
        when(webClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(anyString())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.header(anyString(), anyString())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(SensorSpecDTO.class)).thenReturn(Mono.empty());

        SensorSpecDTO result = sensorService.getSensorSpec(USER_ID, TOKEN);

        assertThat(result).isNull();
    }

    @Test
    void getSensorSpec_shouldPropagateException_whenWebClientThrows() {
        when(webClient.get()).thenThrow(new RuntimeException("Service unavailable"));

        assertThatRuntimeException()
                .isThrownBy(() -> sensorService.getSensorSpec(USER_ID, TOKEN))
                .withMessage("Service unavailable");
    }

    @Test
    void getSensorEndpointsList_shouldSendBearerToken() {
        stubPostWithAuthChain(Mono.just("""
                { "data": { "sensorEndpoints": [] } }
                """));

        sensorService.getSensorEndpointsList(TOKEN, USER_ID, SENSOR_ID, PAGE_SIZE);

        verify(requestBodySpec).header("Authorization", "Bearer " + TOKEN);
    }

    @Test
    void sensorsByUser_shouldSendBearerToken() {
        stubPostWithAuthChain(Mono.just("""
                { "data": { "sensorsByUser": [] } }
                """));

        sensorService.sensorsByUser(TOKEN, USER_ID);

        verify(requestBodySpec).header("Authorization", "Bearer " + TOKEN);
    }

    @Test
    void sensorsByUser_shouldReturnEmptyList_whenNullSensorArray() {
        stubPostWithAuthChain(Mono.just("""
                { "data": { "sensorsByUser": null } }
                """));

        List<SensorEndpointDTO> result = sensorService.sensorsByUser(TOKEN, USER_ID);

        assertThat(result).isEmpty();
    }

    @Test
    void getUserToken_shouldPropagateException_whenWebClientThrows() {
        when(webClient.post()).thenThrow(new RuntimeException("Auth service unavailable"));

        assertThatRuntimeException()
                .isThrownBy(() -> sensorService.getUserToken(new LoginSensorUserDTO("u", "p")))
                .withMessage("Auth service unavailable");
    }

    @Test
    void getSensorSpec_shouldSendBearerToken() {
        when(webClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(anyString())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.header(anyString(), anyString())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(SensorSpecDTO.class)).thenReturn(Mono.empty());

        sensorService.getSensorSpec(SENSOR_ID, TOKEN);

        verify(requestHeadersSpec).header("Authorization", "Bearer " + TOKEN);
    }
}