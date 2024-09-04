package com.sensor.temperaturesensor.service;

import com.sensor.temperaturesensor.dto.SensorEndpointDTO;
import io.micrometer.observation.annotation.Observed;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.Reducer;
import org.apache.kafka.streams.kstream.TimeWindows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.support.serializer.JsonSerializer;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@Slf4j
@Observed(name = "NewSensorValueProcessor")
public class NewSensorValueProcessor {

    @Value(value = "${sensor-manager.topic.sensor-value}")
    private String topic;

    @Value(value = "${sensor-manager.topic.sensor-value-change}")
    private String outputTopic;

    @Value(value = "${sensor-manager.topic.stream-window:60}")
    private long streamWindow;

    private static final Serde<String> STRING_SERDE = Serdes.String();
    private static final Serde<SensorEndpointDTO> SENSOR_ENDPOINT_DTO_SERDE = Serdes.serdeFrom(new JsonSerializer<>(),
            new JsonDeserializer<>(SensorEndpointDTO.class));

    @Autowired
    void buildPipeline(StreamsBuilder streamsBuilder) {

        //List<String> sensorsId = Arrays.asList("sensor.10000db11e_t", "sensor.10000db11e_h", "sensor.10000db501_t");

        //sensorsId.forEach(sensorId -> {
            KStream<String, SensorEndpointDTO> messageStream = streamsBuilder
                    .stream(this.topic, Consumed.with(STRING_SERDE, SENSOR_ENDPOINT_DTO_SERDE));
                    /*.peek((key, sensorEndpointDTO) ->
                            log.info("NewSensorValueProcessor, topic {} key {} value {}", topic, key, sensorEndpointDTO)
                    ).filter((s, sensorEndpointDTO) -> sensorEndpointDTO.getSensorId().equalsIgnoreCase(sensorId));*/

            Reducer<SensorEndpointDTO> sensorValueChange = (sensor1, sensor2) -> sensor1.getSensorId().equals(sensor2.getSensorId()) && !sensor1.getValue().equals(sensor2.getValue()) ? sensor1 : sensor2;

            messageStream.groupByKey()
                    .windowedBy(TimeWindows.ofSizeWithNoGrace(Duration.ofSeconds(streamWindow)))
                    .reduce(sensorValueChange)
                    .toStream()
                    .peek((key, value) -> log.info("reduce topic {} key {} value {}", topic, key, value))
                    .to(this.outputTopic);
        //});
    }

}
