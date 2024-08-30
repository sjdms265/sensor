package com.sensor.temperaturesensor.controller;

import com.sensor.temperaturesensor.dto.SensorEndpointDTO;
import com.sensor.temperaturesensor.model.SensorEndpoint;
import com.sensor.temperaturesensor.service.SensorEndpointService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Limit;
import org.springframework.data.domain.ScrollPosition;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Window;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.graphql.data.query.ScrollSubrange;
import org.springframework.stereotype.Controller;

import java.time.OffsetDateTime;
import java.util.List;

@Controller
@RequiredArgsConstructor
@Slf4j
public class SensorEndpointController {

    @Value("${sensor-manager.pageSize:10}")
    private int pageSize;

    private final SensorEndpointService sensorEndpointService;

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
}
