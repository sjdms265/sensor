package com.sensor.sensorgateway.filter;

public class CustomRequestException  extends RuntimeException {
    public CustomRequestException(String message) {
        super(message);
    }
}
