package com.sensor.sensormcpserver.service;

import com.sensor.sensorcommon.dto.GraphSensorEndpoint;
import com.sensor.sensorcommon.dto.SensorEndpointDTO;
import com.sensor.sensorcommon.dto.SensorSpecDTO;
import com.sensor.sensorcommon.enums.SensorType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.OffsetDateTime;
import java.util.Date;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SensorToolsTest {

    @Mock
    private SensorService sensorService;

    @InjectMocks
    private SensorTools sensorTools;

    private static final String USER_ID = "user123";
    private static final String TOKEN = "jwt-token";
    private static final String SENSOR_ID = "sensor-1";
    private static final int PAGE_SIZE = 50;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(sensorTools, "pageSize", PAGE_SIZE);
    }

    @Test
    void sensorEndpointsBy_shouldReturnTemperatureSensors_whenPatternIsTemperature() {
        // Given
        String pattern = "temperature";
        SensorEndpointDTO sensorEndpoint = new SensorEndpointDTO(USER_ID, SENSOR_ID, 0.0F, new Date(), OffsetDateTime.now());
        SensorSpecDTO sensorSpec = new SensorSpecDTO(SENSOR_ID, "Temperature Sensor", SensorType.TEMPERATURE);
        GraphSensorEndpoint graphEndpoint = new GraphSensorEndpoint(25.5F, new Date());

        when(sensorService.sensorsByUser(TOKEN, USER_ID)).thenReturn(List.of(sensorEndpoint));
        when(sensorService.getSensorSpec(SENSOR_ID, TOKEN)).thenReturn(sensorSpec);
        when(sensorService.getSensorEndpointsList(TOKEN, USER_ID, SENSOR_ID, PAGE_SIZE))
                .thenReturn(List.of(graphEndpoint));

        // When
        Map<String, List<GraphSensorEndpoint>> result = sensorTools.sensorEndpointsBy(USER_ID, pattern, TOKEN);

        // Then
        assertThat(result).hasSize(1);
        assertThat(result).containsKey("temperature");
        assertThat(result.get("temperature")).hasSize(1);
        assertThat(result.get("temperature").getFirst()).isEqualTo(graphEndpoint);

        verify(sensorService).sensorsByUser(TOKEN, USER_ID);
        verify(sensorService).getSensorSpec(SENSOR_ID, TOKEN);
        verify(sensorService).getSensorEndpointsList(TOKEN, USER_ID, SENSOR_ID, PAGE_SIZE);
    }

    @Test
    void sensorEndpointsBy_shouldReturnMultipleSensorTypes_whenPatternContainsMultipleTypes() {
        // Given
        String pattern = "temperature,humidity";
        String humiditySensorId = "sensor-2";

        SensorEndpointDTO tempEndpoint = new SensorEndpointDTO(USER_ID, SENSOR_ID, 25.5F, new Date(), OffsetDateTime.now());
        SensorEndpointDTO humidityEndpoint = new SensorEndpointDTO(USER_ID, humiditySensorId, 60.0F, new Date(), OffsetDateTime.now());

        SensorSpecDTO tempSpec = new SensorSpecDTO(SENSOR_ID, "Temperature Sensor", SensorType.TEMPERATURE);
        SensorSpecDTO humiditySpec = new SensorSpecDTO(humiditySensorId, "Humidity Sensor", SensorType.HUMIDITY);

        GraphSensorEndpoint tempGraph = new GraphSensorEndpoint(25.5F, new Date());
        GraphSensorEndpoint humidityGraph = new GraphSensorEndpoint(60.0F, new Date());

        when(sensorService.sensorsByUser(TOKEN, USER_ID))
                .thenReturn(List.of(tempEndpoint, humidityEndpoint));
        when(sensorService.getSensorSpec(SENSOR_ID, TOKEN)).thenReturn(tempSpec);
        when(sensorService.getSensorSpec(humiditySensorId, TOKEN)).thenReturn(humiditySpec);
        when(sensorService.getSensorEndpointsList(TOKEN, USER_ID, SENSOR_ID, PAGE_SIZE))
                .thenReturn(List.of(tempGraph));
        when(sensorService.getSensorEndpointsList(TOKEN, USER_ID, humiditySensorId, PAGE_SIZE))
                .thenReturn(List.of(humidityGraph));

        // When
        Map<String, List<GraphSensorEndpoint>> result = sensorTools.sensorEndpointsBy(USER_ID, pattern, TOKEN);

        // Then
        assertThat(result).hasSize(2);
        assertThat(result).containsKeys("temperature", "humidity");
        assertThat(result.get("temperature")).hasSize(1);
        assertThat(result.get("humidity")).hasSize(1);

        verify(sensorService, times(2)).sensorsByUser(TOKEN, USER_ID);
        verify(sensorService, times(2)).getSensorSpec(SENSOR_ID, TOKEN);
        verify(sensorService, times(2)).getSensorSpec(humiditySensorId, TOKEN);
    }

    @Test
    void sensorEndpointsBy_shouldReturnEmptyMap_whenNoMatchingSensorType() {
        // Given
        String pattern = "pressure";

        // When
        Map<String, List<GraphSensorEndpoint>> result = sensorTools.sensorEndpointsBy(USER_ID, pattern, TOKEN);

        // Then
        assertThat(result).isEmpty();

        verify(sensorService, times(0)).sensorsByUser(TOKEN, USER_ID);
        verify(sensorService, times(0)).getSensorSpec(SENSOR_ID, TOKEN);
    }

    @Test
    void sensorEndpointsBy_shouldHandleInvalidSensorType_withoutThrowing() {
        // Given
        String pattern = "invalid_type";

        // When
        Map<String, List<GraphSensorEndpoint>> result = sensorTools.sensorEndpointsBy(USER_ID, pattern, TOKEN);

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    void sensorEndpointsBy_shouldSkipDuplicateSensorIds() {
        // Given
        String pattern = "temperature";
        SensorEndpointDTO endpoint1 = new SensorEndpointDTO(USER_ID, SENSOR_ID, 0.0F, new Date(), OffsetDateTime.now());
        SensorEndpointDTO endpoint2 = new SensorEndpointDTO(USER_ID, SENSOR_ID, 0.0F, new Date(), OffsetDateTime.now());
        SensorSpecDTO sensorSpec = new SensorSpecDTO(SENSOR_ID, "Temperature Sensor", SensorType.TEMPERATURE);
        GraphSensorEndpoint graphEndpoint = new GraphSensorEndpoint(25.5F, new Date());

        when(sensorService.sensorsByUser(TOKEN, USER_ID)).thenReturn(List.of(endpoint1, endpoint2));
        when(sensorService.getSensorSpec(SENSOR_ID, TOKEN)).thenReturn(sensorSpec);
        when(sensorService.getSensorEndpointsList(TOKEN, USER_ID, SENSOR_ID, PAGE_SIZE))
                .thenReturn(List.of(graphEndpoint));

        // When
        Map<String, List<GraphSensorEndpoint>> result = sensorTools.sensorEndpointsBy(USER_ID, pattern, TOKEN);

        // Then
        assertThat(result).hasSize(1);
        verify(sensorService, times(1)).getSensorEndpointsList(TOKEN, USER_ID, SENSOR_ID, PAGE_SIZE);
    }

    @Test
    void sensorEndpointsBy_shouldHandleNullSensorSpec() {
        // Given
        String pattern = "temperature";
        SensorEndpointDTO sensorEndpoint = new SensorEndpointDTO(USER_ID, SENSOR_ID, 0.0F, new Date(), OffsetDateTime.now());

        when(sensorService.sensorsByUser(TOKEN, USER_ID)).thenReturn(List.of(sensorEndpoint));
        when(sensorService.getSensorSpec(SENSOR_ID, TOKEN)).thenReturn(null);

        // When
        Map<String, List<GraphSensorEndpoint>> result = sensorTools.sensorEndpointsBy(USER_ID, pattern, TOKEN);

        // Then
        assertThat(result).isEmpty();
        verify(sensorService).getSensorSpec(SENSOR_ID, TOKEN);
        verify(sensorService, times(0)).getSensorEndpointsList(anyString(), anyString(), anyString(), anyInt());
    }

    @Test
    void sensorStatsEndpointsBy_shouldReturnSensorStats() {
        // Given
        int customPageSize = 100;
        GraphSensorEndpoint endpoint1 = new GraphSensorEndpoint(25.5F, new Date());
        GraphSensorEndpoint endpoint2 = new GraphSensorEndpoint(26.0F, new Date());
        List<GraphSensorEndpoint> expectedStats = List.of(endpoint1, endpoint2);

        when(sensorService.getSensorEndpointsList(TOKEN, USER_ID, SENSOR_ID, customPageSize))
                .thenReturn(expectedStats);

        // When
        List<GraphSensorEndpoint> result = sensorTools.sensorStatsEndpointsBy(USER_ID, SENSOR_ID, customPageSize, TOKEN);

        // Then
        assertThat(result).hasSize(2);
        assertThat(result).containsExactly(endpoint1, endpoint2);

        verify(sensorService).getSensorEndpointsList(TOKEN, USER_ID, SENSOR_ID, customPageSize);
    }

    @Test
    void sensorStatsEndpointsBy_shouldReturnEmptyList_whenNoData() {
        // Given
        int customPageSize = 100;

        when(sensorService.getSensorEndpointsList(TOKEN, USER_ID, SENSOR_ID, customPageSize))
                .thenReturn(List.of());

        // When
        List<GraphSensorEndpoint> result = sensorTools.sensorStatsEndpointsBy(USER_ID, SENSOR_ID, customPageSize, TOKEN);

        // Then
        assertThat(result).isEmpty();

        verify(sensorService).getSensorEndpointsList(TOKEN, USER_ID, SENSOR_ID, customPageSize);
    }
}
