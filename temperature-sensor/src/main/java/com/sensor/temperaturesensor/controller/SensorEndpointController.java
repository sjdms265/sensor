package com.sensor.temperaturesensor.controller;

import com.sensor.temperaturesensor.dto.SensorEndpointDTO;
import com.sensor.temperaturesensor.dto.SensorUserDTO;
import com.sensor.temperaturesensor.model.SensorEndpoint;
import com.sensor.temperaturesensor.service.SensorEndpointService;
import lombok.extern.slf4j.Slf4j;
import org.dataloader.BatchLoaderEnvironment;
import org.dataloader.DataLoader;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Limit;
import org.springframework.data.domain.ScrollPosition;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Window;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.graphql.data.method.annotation.SchemaMapping;
import org.springframework.graphql.data.query.ScrollSubrange;
import org.springframework.graphql.execution.BatchLoaderRegistry;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import reactor.core.publisher.Flux;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Controller
@Slf4j
public class SensorEndpointController {

    @Value("${sensor-manager.pageSize:10}")
    private int pageSize;

    private final SensorEndpointService sensorEndpointService;

    public SensorEndpointController(SensorEndpointService sensorEndpointService, BatchLoaderRegistry batchLoaderRegistry) {
        this.sensorEndpointService = sensorEndpointService;
        batchLoaderRegistry.forTypePair(String.class, SensorUserDTO.class).registerBatchLoader(
                (List<String> keys, BatchLoaderEnvironment env) -> {
                    log.debug("Loading sensor users {}", keys);
                    return Flux.just(new SensorUserDTO("name", "username", "password"));
                }
        );
    }

    @QueryMapping
    public List<SensorEndpointDTO> sensorEndpoints(@Argument String userId, @Argument String sensorId,
                                                   @Argument OffsetDateTime fromDate, @Argument OffsetDateTime toDate,
                                                   @Argument Integer pageNumber, @Argument Integer pageSize) {
        return sensorEndpointService.getByUserIdAndSensorIdAndDate(userId, sensorId, fromDate, toDate, pageNumber, pageSize);
    }

    @QueryMapping
    public Window<SensorEndpoint> sensorEndpointList(@Argument String userId, @Argument String sensorId, ScrollSubrange scrollSubrange) {
        ScrollPosition scrollPosition = scrollSubrange.position().orElse(ScrollPosition.offset());
        Limit limit = Limit.of(scrollSubrange.count().orElse(pageSize));
        Sort sort = Sort.by(Sort.Direction.DESC, "date");
        return sensorEndpointService.findByUserIdAndSensorId(userId, sensorId, scrollPosition, limit, sort);
    }

    //https://www.youtube.com/watch?v=wgPAQ0K2e5Y
    @SchemaMapping(typeName = "SensorEndpointDTO")
    public CompletableFuture<SensorUserDTO> sensorUser(SensorEndpointDTO sensorEndpointDTO, DataLoader<String, SensorUserDTO> dataLoader) {
//        SensorUserDTO sensorUserDTO = new SensorUserDTO("name", sensorEndpointDTO.getUserId(), "password");
        return dataLoader.load(sensorEndpointDTO.getUserId());
//        return  Mono.just(sensorUserDTO);
    }

    @GetMapping
    public ResponseEntity<String> home() {
        return ResponseEntity.ok("Hello Temperature-sensor");
    }
}
