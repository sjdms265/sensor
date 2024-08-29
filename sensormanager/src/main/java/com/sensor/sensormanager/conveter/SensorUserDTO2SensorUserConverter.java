package com.sensor.sensormanager.conveter;

import com.sensor.sensormanager.dto.SensorUserDTO;
import com.sensor.sensormanager.model.Role;
import com.sensor.sensormanager.model.SensorUser;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.modelmapper.ModelMapper;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

import java.util.ArrayList;

@RequiredArgsConstructor
@Component
public class SensorUserDTO2SensorUserConverter implements Converter<SensorUserDTO, SensorUser> {

    private final ModelMapper modelMapper;

    @Override
    public SensorUser convert(@NotNull SensorUserDTO sensorUserDTO) {

        SensorUser sensorUser = modelMapper.map(sensorUserDTO, SensorUser.class);
        sensorUser.setRoles(new ArrayList<>());
        sensorUserDTO.roles().forEach(role ->  sensorUser.getRoles().add(new Role(null, role)));
        return sensorUser;
    }
}
