package com.sensor.temperaturesensor.service;

import com.sensor.sensormanager.dto.SensorEndpointDTO;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.utils.Bytes;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.*;
import org.apache.kafka.streams.state.KeyValueStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.support.serializer.JsonSerializer;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@Slf4j
//@Observed(name = "NewTemperatureProcessor")
public class LastSensorValueProcessor {

    @Value(value = "${sensor-manager.topic.sensor-value}")
    private String topic;

    @Value(value = "${sensor-manager.topic.sensor-value-change}")
    private String outputTopic;

    @Value(value = "${sensor-manager.topic.last-sensor-value}")
    private String outputTopicLastValue;

    private final String sensorId = "sensor.10000db11e_t";

    @Value(value = "${sensor-manager.topic.stream-window:60}")
    private long streamWindow;

    private static final Serde<String> STRING_SERDE = Serdes.String();
    private static final Serde<SensorEndpointDTO> SENSOR_ENDPOINT_DTO_SERDE = Serdes.serdeFrom(new JsonSerializer<>(),
            new JsonDeserializer<>(SensorEndpointDTO.class));

    @Autowired
    void buildPipeline(StreamsBuilder streamsBuilder) {

//        setOutputTopicLastValue(streamsBuilder);
    }

    private void reduce(StreamsBuilder streamsBuilder) {
        KStream<String, SensorEndpointDTO> messageStream = streamsBuilder
                .stream(this.topic, Consumed.with(STRING_SERDE, SENSOR_ENDPOINT_DTO_SERDE))
                .peek((key, sensorEndpointDTO) ->
                    log.info("NewTemperatureProcessor, topic {} key {} value {}", topic, key, sensorEndpointDTO)
                ).filter((s, sensorEndpointDTO) -> sensorEndpointDTO.getSensorId().equalsIgnoreCase(sensorId));

        Reducer<SensorEndpointDTO> sensorValueChange = (sensor1, sensor2) -> !sensor1.getValue().equals(sensor2.getValue()) ? sensor1 : sensor2;

        messageStream.groupByKey()
                .windowedBy(TimeWindows.ofSizeWithNoGrace(Duration.ofSeconds(streamWindow)))
                .reduce(sensorValueChange)
                .toStream()
                .peek((key, value) -> log.info("reduce topic {} key {} value {}", topic, key, value))
                .to(this.outputTopic);

    }

    private void aggregator(StreamsBuilder streamsBuilder) {
        KStream<String, SensorEndpointDTO> messageStream = streamsBuilder
                .stream(this.topic, Consumed.with(STRING_SERDE, SENSOR_ENDPOINT_DTO_SERDE))
                .peek((key, value) -> log.debug("aggregator topic {} key {} value {}", topic, key, value));


        final SensorEndpointDTO initialSensorEndpoint = SensorEndpointDTO.builder().value(Float.MIN_VALUE).build();

        Aggregator<String, SensorEndpointDTO, SensorEndpointDTO> sensorValueChange =
                (userId, sensorEndpoint, uniqueTemperature) -> {
                    if(!sensorEndpoint.getValue().equals(initialSensorEndpoint.getValue())) {
                        initialSensorEndpoint.setValue(sensorEndpoint.getValue());
                        return sensorEndpoint;
                    } else {
                        return initialSensorEndpoint;
                    }
                };

        messageStream.groupByKey()
                .windowedBy(TimeWindows.ofSizeWithNoGrace(Duration.ofSeconds(30)))
                .aggregate(() -> initialSensorEndpoint, sensorValueChange, Materialized.with(STRING_SERDE, SENSOR_ENDPOINT_DTO_SERDE))
                .toStream()
                .filter((userId, sensorEndpoint) -> !sensorEndpoint.getValue().equals(Float.MIN_VALUE))
                .to(this.outputTopic);

    }

    private void setOutputTopicLastValue(StreamsBuilder streamsBuilder) {

        KTable<String, SensorEndpointDTO> changeTemperatureTable = streamsBuilder
                .table(this.topic, Materialized.<String, SensorEndpointDTO, KeyValueStore<Bytes, byte[]>>as("last-sensor-value")
                        .withKeySerde(STRING_SERDE)
                        .withValueSerde(SENSOR_ENDPOINT_DTO_SERDE));

        changeTemperatureTable.filter((key, sensorEndpoint) -> sensorEndpoint.getValue() != null)
                .toStream()
                .peek((key, sensorEndpoint) ->
                    log.debug("value {} date {}", sensorEndpoint.getValue(), sensorEndpoint.getDate())
                ).to(this.outputTopicLastValue, Produced.with(STRING_SERDE, SENSOR_ENDPOINT_DTO_SERDE));

    }

}
