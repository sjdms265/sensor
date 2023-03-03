package com.sensor.sensormanager.config;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sensor.sensormanager.dto.SensorEndpointDTO;
import com.sensor.sensormanager.model.SensorUser;
import com.sensor.sensormanager.service.UserService;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.core.MessageProducer;
import org.springframework.integration.mqtt.core.DefaultMqttPahoClientFactory;
import org.springframework.integration.mqtt.core.MqttPahoClientFactory;
import org.springframework.integration.mqtt.inbound.MqttPahoMessageDrivenChannelAdapter;
import org.springframework.integration.mqtt.support.DefaultPahoMessageConverter;
import org.springframework.integration.mqtt.support.MqttHeaders;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageHandler;

@Configuration
@Data
@Slf4j
public class MqqtBeans {

    @Value("${sensormanager.homeassistant.uri}")
    private String serverUri;

    @Value("${sensormanager.homeassistant.userName}")
    private String userName;

    @Value("${sensormanager.homeassistant.password}")
    private String password;

    @Value("${sensormanager.homeassistant.channel}")
    private String channel;

    @Value("${sensormanager.topic.temperature}")
    private String topicTemperature;

    private final UserService userService;

    private final KafkaTemplate<String, SensorEndpointDTO> kafkaTemplate;

    public MqqtBeans(UserService userService, KafkaTemplate<String, SensorEndpointDTO> kafkaTemplate) {
        this.userService = userService;
        this.kafkaTemplate = kafkaTemplate;
    }

    @Bean
    public MqttPahoClientFactory mqqttClientFactory() {

        DefaultMqttPahoClientFactory factory = new DefaultMqttPahoClientFactory();
        MqttConnectOptions options = new MqttConnectOptions();

        options.setServerURIs(new String[] {this.serverUri});
        options.setUserName(this.userName);
        options.setPassword(this.password.toCharArray());
        options.setCleanSession(true);

        factory.setConnectionOptions(options);

        return factory;
    }

    @Bean
    public MessageChannel mqttInputChannel() {
        return new DirectChannel();
    }

    @Bean
    public MessageProducer inbound() {
        MqttPahoMessageDrivenChannelAdapter adapter = new MqttPahoMessageDrivenChannelAdapter("serverIn",
                mqqttClientFactory(), this.channel);
        adapter.setCompletionTimeout(5000);
        adapter.setConverter(new DefaultPahoMessageConverter());
        adapter.setQos(2);
        adapter.setOutputChannel(mqttInputChannel());
        return adapter;
    }

    @Bean
    @ServiceActivator(inputChannel = "mqttInputChannel")
    public MessageHandler handler() {

        return message -> {
            String topic = String.valueOf(message.getHeaders().get(MqttHeaders.RECEIVED_TOPIC)).toUpperCase();
            String payload = message.getPayload().toString();

            //SensorType sensorType = SensorType.valueOf(topic);

            ObjectMapper om = new ObjectMapper();

            try {
                SensorEndpointDTO sensorEndpointDTO = om.readValue(payload, SensorEndpointDTO.class);
                log.info("{} Topic {} message {}",  sensorEndpointDTO.getDate(), topic, message.getPayload());

                SensorUser sensorUser = userService.getUser(sensorEndpointDTO.getUserId());

                this.kafkaTemplate.send(this.topicTemperature, sensorUser.getUsername(), sensorEndpointDTO);

            } catch (JsonProcessingException e) {
                throw new IllegalStateException(e);
            }

        };

    }

//    @Bean
//    public MessageChannel mqttOutputChannel() {
//        return new DirectChannel();
//    }
//
//    @Bean
//    @ServiceActivator(inputChannel = "mqttOutputChannel")
//    public MessageHandler mqttOutbound() {
//
//        MqttPahoMessageHandler messageHandler = new MqttPahoMessageHandler("serverOut", mqqttClientFactory());
//        messageHandler.setAsync(true);
//        messageHandler.setDefaultTopic("test");
//        return  messageHandler;
//    }


}
