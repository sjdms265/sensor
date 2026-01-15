package com.sensor.sensorgateway.filter;

import org.springframework.boot.autoconfigure.web.WebProperties;
import org.springframework.boot.autoconfigure.web.reactive.error.AbstractErrorWebExceptionHandler;
import org.springframework.boot.web.error.ErrorAttributeOptions;
import org.springframework.boot.web.reactive.error.ErrorAttributes;
import org.springframework.context.ApplicationContext;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerCodecConfigurer;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.server.RequestPredicates;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;

import java.util.Map;

@Component
@Order(Integer.MIN_VALUE)
/*
 * https://www.baeldung.com/spring-cloud-global-exception-handling
 */
class CustomGlobalExceptionHandler extends AbstractErrorWebExceptionHandler {

    public CustomGlobalExceptionHandler(final ErrorAttributes customErrorAttributes,
                                        final WebProperties.Resources resources,
                                        final ApplicationContext applicationContext,
                                        final ServerCodecConfigurer configurer) {
        super(customErrorAttributes, resources, applicationContext);
        setMessageReaders(configurer.getReaders());
        setMessageWriters(configurer.getWriters());
    }

    @Override
    protected RouterFunction<ServerResponse> getRoutingFunction(ErrorAttributes errorAttributes) {
        return RouterFunctions.route(RequestPredicates.all(), this::renderErrorResponse);
    }

    private Mono<ServerResponse> renderErrorResponse(ServerRequest request) {

        ErrorAttributeOptions options = ErrorAttributeOptions.of(ErrorAttributeOptions.Include.MESSAGE);
        Map<String, Object> errorPropertiesMap = getErrorAttributes(request, options);
        Throwable throwable = getError(request);
        HttpStatusCode httpStatus = determineHttpStatus(throwable);

        errorPropertiesMap.put("status", httpStatus.value());
        errorPropertiesMap.remove("error");

        return ServerResponse.status(httpStatus)
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(errorPropertiesMap));
    }

    private HttpStatusCode determineHttpStatus(Throwable throwable) {

        if (throwable instanceof ResponseStatusException) {
            return ((ResponseStatusException) throwable).getStatusCode();
        } else if (throwable instanceof CustomRequestException) {
            return HttpStatus.UNAUTHORIZED;
        } else {
            return HttpStatus.INTERNAL_SERVER_ERROR;
        }
    }
}
