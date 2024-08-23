package com.sensor.temperaturesensor.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.annotation.EnableKafkaStreams;

@Configuration
@EnableKafka
@EnableKafkaStreams
public class KafkaStreamConfig {

    /*@Value(value = "${spring.kafka.bootstrap-servers}")
    private String bootstrapAddress;

    @Value(value = "${sensor-manager.topic.sensor-value}")
    private String topic;

    @Value(value = "${sensor-manager.topic.sensor-value-change}")
    private String outputTopic;

    @Bean(name = KafkaStreamsDefaultConfiguration.DEFAULT_STREAMS_CONFIG_BEAN_NAME)
    KafkaStreamsConfiguration kStreamsConfig() {

        Serde<SensorEndpointDTO> sensorEndpointDTOSerde = Serdes.serdeFrom(new JsonSerializer<>(), new JsonDeserializer<>());

        Map<String, Object> props = new HashMap<>();
        props.put(APPLICATION_ID_CONFIG, "streams-app");
        props.put(BOOTSTRAP_SERVERS_CONFIG, bootstrapAddress);
        props.put(DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass().getName());
        props.put(DEFAULT_VALUE_SERDE_CLASS_CONFIG, sensorEndpointDTOSerde.getClass().getName());
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "json");

        return new KafkaStreamsConfiguration(props);
    }

    @Bean
    NewTopic inputTopic() {
        return TopicBuilder.name(this.topic)
            .partitions(1)
            .replicas(1)
            .build();
    }

    @Bean
    NewTopic outputTopic() {
        return TopicBuilder.name(this.outputTopic)
                .partitions(1)
                .replicas(1)
                .build();
    }*/

}
