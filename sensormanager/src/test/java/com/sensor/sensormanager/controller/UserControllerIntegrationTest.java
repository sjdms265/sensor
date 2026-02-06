package com.sensor.sensormanager.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sensor.sensormanager.dto.RoleToUserForm;
import com.sensor.sensormanager.model.Role;
import com.sensor.sensormanager.model.SensorUser;
import com.sensor.sensormanager.service.UserService;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/*
 * https://www.baeldung.com/spring-security-integration-tests
 * https://github.com/eugenp/tutorials/blob/master/spring-boot-modules/spring-boot-security/src/test/java/com/baeldung/integrationtesting/SecuredControllerSpringBootIntegrationTest.java
 */
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestInstance(TestInstance.Lifecycle.PER_CLASS) //Required for @AfterAll
class UserControllerIntegrationTest {

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private ObjectMapper mapper;

    @Autowired
    private UserService userService;

    private MockMvc mvc;

    @BeforeEach
    void setup() {
        mvc = MockMvcBuilders.webAppContextSetup(context)
                .apply(springSecurity())
                .build();
    }

    @Test
    @WithMockUser(username="admin", roles = {"ADMIN"})
    void getUsersRoleAdmin() throws Exception {
        mvc.perform(get(BaseController.BASE_PATH + UserController.ADMIN_USERS_PATH).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is2xxSuccessful());
    }

    @Test
    @WithMockUser(username="sjdms265")
    void getUsersRoleUser() throws Exception {
        mvc.perform(get(BaseController.BASE_PATH + UserController.USERS_PATH).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is4xxClientError());
    }

    @Test
    void getUsersNoAuthenticated() throws Exception {
        mvc.perform(get(BaseController.BASE_PATH + UserController.USERS_PATH).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is4xxClientError());
    }

    @Test
    @WithMockUser(username="sjdms265", password = "1234")
    void saveUser() throws Exception {

        SensorUser sensorUser = SensorUser.builder().username("testUser").password("1234").name("testName").build();
        mvc.perform(
                post(BaseController.BASE_PATH + UserController.USERS_PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(sensorUser))
                        .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().is2xxSuccessful());
    }

    @Test
    @WithMockUser(username="admin", roles = {"ADMIN"})
    void saveUserAdmin() throws Exception {
        SensorUser sensorUser = SensorUser.builder().username("testUser").password("1234").name("testName").build();
        mvc.perform(post(BaseController.BASE_PATH + UserController.USERS_PATH).contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(sensorUser))
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is2xxSuccessful());
    }

    @Test
    void saveUserNoAuthenticated() throws Exception {
        SensorUser sensorUser = SensorUser.builder().username("testUser").password("1234").name("testName").build();
        mvc.perform(post(BaseController.BASE_PATH + UserController.USERS_PATH).contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(sensorUser))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is4xxClientError());
    }

    @Test
    @WithMockUser(username="user")
    void saveRole() throws Exception {
        Role role = Role.builder().name("testRole").build();
        mvc.perform(post(BaseController.BASE_PATH + UserController.ROLES_PATH).contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(role))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is2xxSuccessful());
    }

    @Test
    @WithMockUser(username="admin", roles = {"ADMIN"})
    void saveRoleAdmin() throws Exception {
        Role role = Role.builder().name("testRole").build();
        mvc.perform(post(BaseController.BASE_PATH + UserController.ROLES_PATH).contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(role))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is2xxSuccessful());
    }

    @Test
    void saveRoleNoAuthenticated() throws Exception {
        Role role = Role.builder().name("testRole").build();
        mvc.perform(post(BaseController.BASE_PATH + UserController.ROLES_PATH).contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(role))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is4xxClientError());
    }

    @Test
    @WithMockUser(username="user")
    void addRole() throws Exception {
        RoleToUserForm roleToUserForm = new RoleToUserForm("testUser", "testRole");
        mvc.perform(post(BaseController.BASE_PATH + UserController.ADD_ROLE_TO_USER_PATH).contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(roleToUserForm))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is2xxSuccessful());
    }

    @Test
    @WithMockUser(username="admin", roles = {"ADMIN"})
    void addRoleAdmin() throws Exception {
        RoleToUserForm roleToUserForm = new RoleToUserForm("testUser", "testRole");
        mvc.perform(post(BaseController.BASE_PATH + UserController.ADD_ROLE_TO_USER_PATH).contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(roleToUserForm))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is2xxSuccessful());
    }

    @Test
    void addRoleNoAuthenticated() throws Exception {
        RoleToUserForm roleToUserForm = new RoleToUserForm("testUser", "testRole");
        mvc.perform(post(BaseController.BASE_PATH + UserController.ADD_ROLE_TO_USER_PATH).contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(roleToUserForm))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is4xxClientError());
    }

    @Test
    @WithMockUser(username="admin")
    @Disabled
    void refreshToken() throws Exception {

        //https://jwt.io/ change signature secret
        /*{
            "sub": "1234567890",
                "name": "John Doe",
                "iat": 1516239022,
                "roles": [
                        "ADMIN"
              ]
        }*/

        mvc.perform(get(BaseController.BASE_PATH + UserController.REFRESH_TOKEN).with(request -> {
                    request.addHeader(HttpHeaders.AUTHORIZATION, "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyLCJyb2xlcyI6WyJBRE1JTiJdfQ.oW8uC8uyL4nZSjcDGRkW3ZHoEoHShPD7ft0cppgvQe4");
                    return request;
                }))
                .andExpect(status().is2xxSuccessful());
    }

    @AfterAll
    public void tearDown() {

        userService.deleteUser("testUser");
        userService.deleteRole("testRole");


    }
}
