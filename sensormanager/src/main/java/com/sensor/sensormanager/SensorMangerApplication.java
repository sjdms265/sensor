package com.sensor.sensormanager;

import com.sensor.sensormanager.model.Role;
import com.sensor.sensormanager.model.SensorUser;
import com.sensor.sensormanager.service.UserService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.ArrayList;

@SpringBootApplication
public class SensorMangerApplication {

    public static void main(String[] args) {
        SpringApplication.run(SensorMangerApplication.class, args);
    }

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    CommandLineRunner run(UserService userService) {
        return args -> {
            userService.saveRole(new Role(null, "ROLE_USER"));
            userService.saveRole(new Role(null, "ROLE_ADMIN"));

            SensorUser sensorUser = userService.saveUser(new SensorUser(null, "sjdms265", "sjdms265", "1234", new ArrayList<>()));
            userService.saveUser(new SensorUser(null, "admin", "admin", "1234", new ArrayList<>()));

            userService.addRoleToUser("sjdms265", "ROLE_USER");
            userService.addRoleToUser("admin", "ROLE_ADMIN");
            userService.addRoleToUser("admin", "ROLE_USER");

        };
    }

    /*@Bean
    public MessageChannel mqttInputChannel() {
        return new DirectChannel();
    }

    @Bean
    @ServiceActivator(inputChannel = "mqttInputChannel")
    public MessageHandler handler() {

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyy-MM-dd HH:m:mm:ss");

        return message -> {
            String topic = message.getHeaders().get(MqttHeaders.RECEIVED_TOPIC).toString();
            System.out.println(String.format("%s Message from topic %s: %s", dateFormat.format(new Date()) , topic, message.getPayload()));
            //log.debug("{} Topic {} message {}",  new Date().getTime(), topic, message.getPayload()); //fixme
        };

    }*/

}
