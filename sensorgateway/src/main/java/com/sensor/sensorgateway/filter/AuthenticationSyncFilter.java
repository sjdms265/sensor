package com.sensor.sensorgateway.filter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.HashMap;
import java.util.Objects;

/*
    https://www.youtube.com/watch?v=MWvnmyLRUik
 */
@Component
@Slf4j
//@RequiredArgsConstructor
public class AuthenticationSyncFilter extends AbstractGatewayFilterFactory<AuthenticationSyncFilter.Config> {

    private final RouteValidator routeValidator;

    //https://docs.spring.io/spring-cloud-commons/reference/spring-cloud-commons/common-abstractions.html#rest-client-loadbalancer-client
    private final WebClient.Builder webClientBuilder;

    public AuthenticationSyncFilter(RouteValidator routeValidator, WebClient.Builder webClientBuilder) {
        super(Config.class);
        this.routeValidator = routeValidator;
        this.webClientBuilder = webClientBuilder;
    }
    @Override
    public GatewayFilter apply(Config config) {

        log.debug("Starting AuthenticationSyncFilter");

        return ((exchange, chain) -> {

            if(routeValidator.isAuth.test(exchange.getRequest())) {

                if(!exchange.getRequest().getHeaders().containsKey(HttpHeaders.AUTHORIZATION)) {
                    throw new RuntimeException("Authorization header not present");
                }

                String token = Objects.requireNonNull(exchange.getRequest().getHeaders().get(HttpHeaders.AUTHORIZATION)).getFirst();
                if(token.startsWith("Bearer ")) {
                    token = token.substring("Bearer ".length());
                }

                log.debug("Token {}", token);

                //https://www.baeldung.com/spring-cloud-custom-gateway-filters#3-chaining-requests-to-other-services
                return webClientBuilder.build().get().
                       uri("lb://SENSORMANAGER/sensormanager/api/auth/validateToken/" + token).
                       exchangeToMono(clientResponse -> {
                           log.debug("client response {}", clientResponse.statusCode());

                           if (clientResponse.statusCode().is2xxSuccessful()) return clientResponse.bodyToMono(HashMap.class);

                           throw new CustomRequestException("Not authorized");
                       }).
                       map(hashMap -> {
                           exchange.getRequest().mutate().build();
                           return exchange;
                       }).
                       flatMap(chain::filter);

            }
            return chain.filter(exchange);
        });
    }

    public static class Config {

    }
}
