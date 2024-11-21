package com.sensor.sensormanager.conveter;

import com.sensor.sensormanager.dto.SensorUserDTO;
import com.sensor.sensormanager.model.Role;
import com.sensor.sensormanager.model.SensorUser;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.BeanUtils;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

import java.util.ArrayList;

@RequiredArgsConstructor
@Component
public class SensorUserDTO2SensorUserConverter implements Converter<SensorUserDTO, SensorUser> {

    @Override
    public SensorUser convert(@NotNull SensorUserDTO sensorUserDTO) {

        SensorUser sensorUser = SensorUser.builder().build();
        BeanUtils.copyProperties(sensorUserDTO, sensorUser);
        sensorUser.setRoles(new ArrayList<>());
        sensorUserDTO.roles().forEach(role ->  sensorUser.getRoles().add(new Role(null, role)));
        return sensorUser;
    }
}
