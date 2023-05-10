package com.sensor.sensormanager.observability;

import io.micrometer.observation.ObservationRegistry;
import io.micrometer.observation.aop.ObservedAspect;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class ObservedAspectConfiguration {

    private final SimpleLoggingHandler simpleLoggingHandler;

    @Bean
    public ObservedAspect observedAspect(ObservationRegistry observationRegistry) {
        observationRegistry.observationConfig().observationHandler(simpleLoggingHandler);

        return new ObservedAspect(observationRegistry);
    }
}
