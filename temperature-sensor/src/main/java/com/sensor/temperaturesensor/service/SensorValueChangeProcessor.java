package com.sensor.temperaturesensor.service;

import com.sensor.temperaturesensor.dto.SensorEndpointDTO;
import com.sensor.temperaturesensor.model.SensorEndpoint;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.Consumed;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.support.serializer.JsonSerializer;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class SensorValueChangeProcessor {

    public static final Serde<String> STRING_SERDE = Serdes.String();
    public static final Serde<SensorEndpointDTO> SENSOR_ENDPOINT_DTO_SERDE = Serdes.serdeFrom(new JsonSerializer<>(),
            new JsonDeserializer<>(SensorEndpointDTO.class));

    private final SensorEndpointService sensorEndpointService;

    @Value("${sensor-manager.topic.sensor-value-change}")
    private String topic;

    @Autowired
    void buildPipeline(StreamsBuilder streamsBuilder) {

        streamsBuilder
            .stream(topic, Consumed.with(STRING_SERDE, SENSOR_ENDPOINT_DTO_SERDE))
            .peek((key, sensorEndpointDTO) -> {
                log.debug("Possible value changed, topic {} key {} value {}", topic, key, sensorEndpointDTO);
                SensorEndpoint sensorEndpoint = SensorEndpoint.builder().userId(sensorEndpointDTO.getUserId()).
                        sensorId(sensorEndpointDTO.getSensorId()).value(sensorEndpointDTO.getValue()).date(sensorEndpointDTO.getDate()).build();

                sensorEndpointService.recordSensorEndpoint(sensorEndpoint);
            });

    }

}
