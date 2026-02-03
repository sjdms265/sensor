package com.sensor.sensormanager.dto;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sensor.sensorcommon.dto.SensorEndpointDTO;
import lombok.RequiredArgsConstructor;
import org.apache.camel.Converter;
import org.apache.camel.TypeConverters;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class SensorEndpointDTOTypeConverter implements TypeConverters {

    private final ObjectMapper mapper;

    @Converter
    public byte[] myPackageToByteArray(SensorEndpointDTO source) {
        try {
            return mapper.writeValueAsBytes(source);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    @Converter
    public SensorEndpointDTO byteArrayToMyPackage(byte[] source) {
        try {
            return mapper.readValue(source, SensorEndpointDTO.class);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
