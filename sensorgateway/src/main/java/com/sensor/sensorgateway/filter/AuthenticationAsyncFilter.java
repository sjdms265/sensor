package com.sensor.sensorgateway.filter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.HashMap;

/*
    https://www.youtube.com/watch?v=MWvnmyLRUik
 */
@Component
@Slf4j
//@RequiredArgsConstructor
public class AuthenticationAsyncFilter extends AbstractGatewayFilterFactory<AuthenticationAsyncFilter.Config> {

    private final RouteValidator routeValidator;

    private final WebClient.Builder webClientBuilder;

    public AuthenticationAsyncFilter(RouteValidator routeValidator, WebClient.Builder webClientBuilder) {
        super(Config.class);
        this.routeValidator = routeValidator;
        this.webClientBuilder = webClientBuilder;
    }
    @Override
    public GatewayFilter apply(Config config) {

        log.debug("Starting AuthenticationFilter");

        return ((exchange, chain) -> {

            if(routeValidator.isAuth.test(exchange.getRequest())) {

                if(!exchange.getRequest().getHeaders().containsKey(HttpHeaders.AUTHORIZATION)) {
                    throw new RuntimeException("Authorization header not present");
                }

                String token = exchange.getRequest().getHeaders().get(HttpHeaders.AUTHORIZATION).getFirst();
                if(token.startsWith("Bearer ")) {
                    token = token.substring("Bearer ".length());
                }

                log.debug("Token {}", token);

                Mono<HashMap> response = webClientBuilder.build().get().
                        uri("lb://SENSORMANAGER/sensormanager/api/auth/validateToken/" + token).retrieve().
                        bodyToMono(HashMap.class).onErrorResume(Mono::error);

                response.subscribe(map -> {
                    log.info("Response body: {}", map);

                    if(map.containsKey("success")) {
                        log.debug("Valid token: {}", map.get("success"));
                    } else {
                        log.debug("Invalid token: {}", map.get("error"));
                        throw new RuntimeException("Invalid token" + map.get("error"));
                    }
                });

            }

            return chain.filter(exchange);
        });
    }

    public static class Config {

    }
}
