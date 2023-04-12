package com.sensor.temperaturesensor.service;

import com.sensor.sensormanager.dto.SensorEndpointDTO;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.Aggregator;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.KTable;
import org.apache.kafka.streams.kstream.Materialized;
import org.apache.kafka.streams.kstream.Produced;
import org.apache.kafka.streams.kstream.Reducer;
import org.apache.kafka.streams.kstream.TimeWindows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.support.serializer.JsonSerializer;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
public class NewHumidityProcessor {

    @Value(value = "${sensor-manager.topic.sensor-value}")
    private String topic;

    @Value(value = "${sensor-manager.topic.sensor-value-change}")
    private String outputTopic;

    private final String sensorId = "sensor.10000db11e_h";

    private static final Serde<String> STRING_SERDE = Serdes.String();
    private static final Serde<SensorEndpointDTO> SENSOR_ENDPOINT_DTO_SERDE = Serdes.serdeFrom(new JsonSerializer<>(),
            new JsonDeserializer<>(SensorEndpointDTO.class));

    @Autowired
    void buildPipeline(StreamsBuilder streamsBuilder) {

        reduce(streamsBuilder);
    }

    private void reduce(StreamsBuilder streamsBuilder) {
        KStream<String, SensorEndpointDTO> messageStream = streamsBuilder
                .stream(this.topic, Consumed.with(STRING_SERDE, SENSOR_ENDPOINT_DTO_SERDE))
                .peek((key, value) -> {
                    if(value.getSensorId().equalsIgnoreCase(sensorId)) {
                        log.info("reduce topic {} key {} value {}", topic, key, value);
                    }
                });

        Reducer<SensorEndpointDTO> valueChange = (sensor1, sensor2) -> !sensor1.getValue().equals(sensor2.getValue())
                && sensor1.getSensorId().equalsIgnoreCase(sensorId) ? sensor1 : sensor2;

        messageStream.groupByKey()
                .windowedBy(TimeWindows.ofSizeWithNoGrace(Duration.ofSeconds(30)))
                .reduce(valueChange)
                .toStream()
                .to(this.outputTopic);

    }

    private void aggregator(StreamsBuilder streamsBuilder) {
        KStream<String, SensorEndpointDTO> messageStream = streamsBuilder
                .stream(this.topic, Consumed.with(STRING_SERDE, SENSOR_ENDPOINT_DTO_SERDE))
                .peek((key, value) -> log.info("aggregator topic {} key {} value {}", topic, key, value));


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

    private void ktable(StreamsBuilder streamsBuilder) {
                Map<String, SensorEndpointDTO> sensorEndpointMap = new HashMap<>();

        KTable<String, SensorEndpointDTO> changeTemperatureTable = streamsBuilder
                .table(this.topic, Consumed.with(STRING_SERDE, SENSOR_ENDPOINT_DTO_SERDE));
//                .table(this.topic, Materialized.<String, SensorEndpointDTO, KeyValueStore<Bytes, byte[]>>as("ktable-changeTemperature")
//                        .withKeySerde(STRING_SERDE)
//                        .withValueSerde(SENSOR_ENDPOINT_DTO_SERDE));

        changeTemperatureTable.mapValues(sensorEndpoint -> {
                    log.debug(String.format("size %s value %s date %s", sensorEndpointMap.size(), sensorEndpoint.getValue(), sensorEndpoint.getDate()));
                    sensorEndpointMap.put(String.valueOf(sensorEndpoint.getValue()), sensorEndpoint);
                    return sensorEndpoint;
                })
                .filter((key, sensorEndpoint) -> sensorEndpointMap.get(String.valueOf(sensorEndpoint.getValue())) != null)
                .toStream()
                .peek((key, sensorEndpoint) ->
                    log.debug("size %s value %s date %s exists %b", sensorEndpointMap.size(),
                            sensorEndpoint.getValue(),
                            sensorEndpoint.getDate(), sensorEndpointMap.get(String.valueOf(sensorEndpoint.getValue())) == null)
                )
                        .to(this.outputTopic, Produced.with(STRING_SERDE, SENSOR_ENDPOINT_DTO_SERDE));


//        streamsBuilder
//                .table(this.topic, Consumed.with(STRING_SERDE, SENSOR_ENDPOINT_DTO_SERDE))
//                .mapValues(sensorEndpoint -> sensorEndpointMap.put(sensorEndpoint.getValue(), sensorEndpoint));
//
//        sensorEndpointMap.keySet().stream().peek(key -> System.out.println(String.format("value %s date %s", key, sensorEndpointMap.get(key).getDate())));


   /*     KTable<String, Long> wordCounts = messageStream
            .mapValues((ValueMapper<String, String>) String::toLowerCase)
            .flatMapValues(value -> Arrays.asList(value.split("\\W+")))
            .groupBy((key, word) -> word, Grouped.with(STRING_SERDE, STRING_SERDE))
            .count(Materialized.as("counts"));

        wordCounts.toStream().to("output-topic");*/
    }

}
