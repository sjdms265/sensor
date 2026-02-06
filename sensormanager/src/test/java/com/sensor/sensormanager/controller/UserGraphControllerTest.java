package com.sensor.sensormanager.controller;

import com.sensor.sensormanager.model.SensorUser;
import com.sensor.sensormanager.service.UserService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.graphql.GraphQlTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.graphql.test.tester.GraphQlTester;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@GraphQlTest(UserGraphController.class)
class UserGraphControllerTest {

    @MockBean
    private UserService userService;

    @Autowired
    private GraphQlTester graphQlTester;

    @Test
    void users() {

        SensorUser sensorUser = SensorUser.builder().name("name").username("username").id("id").build();
        SensorUser sensorUser2 = SensorUser.builder().name("name2").username("username2").id("id2").build();
        Mockito.when(userService.getUsers()).thenReturn(List.of(sensorUser, sensorUser2));

        String document = """
                {
                    users {
                        id
                        name
                        roles {
                            name
                        }
                    }
                }
                """;


        graphQlTester.document(document).execute().path("users").entityList(SensorUser.class).hasSize(2);
    }

    @Test
    void userByUserName() {

        SensorUser sensorUser = SensorUser.builder().name("name").username("username").id("id").build();
        Mockito.when(userService.getByUsername(Mockito.anyString())).thenReturn(sensorUser);

        String document = """
                 {
                    userByUserName(username: "admin") {
                        id
                        name
                        roles {
                            name
                        }
                    }
                }
                """;

        SensorUser returnedSensorUser = graphQlTester.document(document).variable("username", "admin").execute()
                .path("userByUserName").entity(SensorUser.class).get();
        assertNotNull(returnedSensorUser);
    }
}
