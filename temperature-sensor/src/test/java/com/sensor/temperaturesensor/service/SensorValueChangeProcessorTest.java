package com.sensor.temperaturesensor.service;

import com.sensor.sensorcommon.dto.SensorEndpointDTO;
import com.sensor.temperaturesensor.model.SensorEndpoint;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.TestInputTopic;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.TopologyTestDriver;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Date;
import java.util.Properties;
import java.util.concurrent.CompletableFuture;

/*
    https://www.baeldung.com/spring-boot-kafka-streams
 */
class SensorValueChangeProcessorTest {


    private SensorValueChangeProcessor sensorValueChangeProcessor;

    @Mock
    private SensorEndpointService sensorEndpointService;

    private final String inputTopicName = "sensor-value-change";

    @BeforeEach
    void setUp() throws Exception {
        try (final AutoCloseable autoCloseable = MockitoAnnotations.openMocks(this)) {
            Assertions.assertNotNull(sensorEndpointService);
            sensorValueChangeProcessor = new SensorValueChangeProcessor(sensorEndpointService);
            ReflectionTestUtils.setField(sensorValueChangeProcessor, "topic", inputTopicName);
        }

    }

    @Test
    void buildPipeline() {

        Mockito.when(sensorEndpointService.recordSensorEndpoint(Mockito.any())).thenReturn(CompletableFuture.completedFuture(new SensorEndpoint()));

        final StreamsBuilder streamsBuilder = new StreamsBuilder();
        sensorValueChangeProcessor.buildPipeline(streamsBuilder);
        Topology topology = streamsBuilder.build();

        try(final TopologyTestDriver topologyTestDriver = new TopologyTestDriver(topology, new Properties())) {
            final TestInputTopic<String, SensorEndpointDTO> sensorEndpointDTOTestInputTopic =
                    topologyTestDriver.createInputTopic(inputTopicName, SensorValueChangeProcessor.STRING_SERDE.serializer(),
                            SensorValueChangeProcessor.SENSOR_ENDPOINT_DTO_SERDE.serializer());

            SensorEndpointDTO sensorEndpointDTO = SensorEndpointDTO.builder().sensorId("sensorId").value(1.0F).date(new Date()).userId("userId").build();
            sensorEndpointDTOTestInputTopic.pipeInput(sensorEndpointDTO.getUserId() + "-" + sensorEndpointDTO.getSensorId(), sensorEndpointDTO);
        }

        Mockito.verify(sensorEndpointService, Mockito.times(1)).recordSensorEndpoint(Mockito.any());
    }
}
