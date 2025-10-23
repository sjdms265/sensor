package com.sensor.sensormanager.conveter;

import com.sensor.sensormanager.dto.SensorUserDTO;
import com.sensor.sensormanager.model.SensorUser;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockitoAnnotations;


import java.util.Collection;
import java.util.List;

class SensorUserDTO2SensorUserConverterTest {

    private SensorUserDTO2SensorUserConverter converter;

    @BeforeEach
    void setUp() throws Exception {

        try (final AutoCloseable autoCloseable = MockitoAnnotations.openMocks(this)) {
            converter = new SensorUserDTO2SensorUserConverter();
        }
    }

    @Test
    void convert() {

        Collection<String> roles = List.of("name");
        SensorUser sensorUser = converter.convert(new SensorUserDTO("name", "username",
                "password", roles));
        Assertions.assertNotNull(sensorUser);
        Assertions.assertEquals("name", sensorUser.getName());
        Assertions.assertEquals("username", sensorUser.getUsername());
        Assertions.assertEquals("password", sensorUser.getPassword());
        Assertions.assertEquals(roles.size(), sensorUser.getRoles().size());
    }
}