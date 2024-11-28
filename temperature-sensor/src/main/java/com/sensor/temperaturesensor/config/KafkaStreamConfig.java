package com.sensor.temperaturesensor.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.annotation.EnableKafkaStreams;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
@EnableKafka
@EnableKafkaStreams
public class KafkaStreamConfig {

    @Value(value = "${sensor-manager.topic.sensor-value}")
    private String topic;

    @Value(value = "${sensor-manager.topic.sensor-value-change}")
    private String outputTopic;

    /*
        Topics need to be created on a fresh install
     */
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
    }

}
