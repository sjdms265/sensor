package com.sensor.sensorgateway.filter;

import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.function.Predicate;

@Component
public class RouteValidator {

    public static final List<String> noAuthEndpoints = List.of("/auth", "/eureka");

    public Predicate<ServerHttpRequest> isAuth = request -> noAuthEndpoints.stream().noneMatch(uri -> request.getURI().
            getPath().contains(uri));
}
