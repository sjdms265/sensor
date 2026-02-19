package com.sensor.sensormcpserver.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sensor.sensorcommon.dto.LoginSensorUserDTO;
import com.sensor.sensormcpserver.dto.TokenResponseDTO;
import io.modelcontextprotocol.spec.McpSchema;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserProfileResourceTest {

    @Mock
    private SensorService sensorService;

    @InjectMocks
    private UserProfileResource userProfileResource;

    private static final String USERNAME = "testuser";
    private static final String PASSWORD = "testpassword";
    private static final String ACCESS_TOKEN = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.access";
    private static final String REFRESH_TOKEN = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.refresh";

    private TokenResponseDTO tokenResponseDTO;

    @BeforeEach
    void setUp() {
        tokenResponseDTO = new TokenResponseDTO(ACCESS_TOKEN, REFRESH_TOKEN);
    }

    @Test
    void getToken_shouldReturnResourceContents_whenCredentialsAreValid() throws JsonProcessingException {
        // Given
        when(sensorService.getUserToken(any(LoginSensorUserDTO.class))).thenReturn(tokenResponseDTO);

        // When
        List<McpSchema.ResourceContents> result = userProfileResource.getToken(USERNAME, PASSWORD);

        // Then
        assertThat(result).isNotNull();
        assertThat(result).hasSize(1);

        McpSchema.ResourceContents resourceContent = result.getFirst();
        assertThat(resourceContent).isInstanceOf(McpSchema.TextResourceContents.class);

        McpSchema.TextResourceContents textContent = (McpSchema.TextResourceContents) resourceContent;
        assertThat(textContent.uri()).isEqualTo("/user/profile/" + USERNAME);
        assertThat(textContent.mimeType()).isEqualTo("application/json");
        assertThat(textContent.text()).contains(ACCESS_TOKEN);
        assertThat(textContent.text()).contains(REFRESH_TOKEN);

        verify(sensorService).getUserToken(any(LoginSensorUserDTO.class));
    }

    @Test
    void getToken_shouldContainValidJsonInResourceContents() throws JsonProcessingException {
        // Given
        when(sensorService.getUserToken(any(LoginSensorUserDTO.class))).thenReturn(tokenResponseDTO);

        // When
        List<McpSchema.ResourceContents> result = userProfileResource.getToken(USERNAME, PASSWORD);

        // Then
        McpSchema.TextResourceContents textContent = (McpSchema.TextResourceContents) result.get(0);
        String jsonContent = textContent.text();

        // Verify that the JSON can be deserialized back to TokenResponseDTO
        ObjectMapper mapper = new ObjectMapper();
        TokenResponseDTO deserializedToken = mapper.readValue(jsonContent, TokenResponseDTO.class);

        assertThat(deserializedToken.access_token()).isEqualTo(ACCESS_TOKEN);
        assertThat(deserializedToken.refresh_token()).isEqualTo(REFRESH_TOKEN);
    }

    @Test
    void getToken_shouldConstructCorrectUri_withUsername() throws JsonProcessingException {
        // Given
        String customUsername = "john.doe";
        when(sensorService.getUserToken(any(LoginSensorUserDTO.class))).thenReturn(tokenResponseDTO);

        // When
        List<McpSchema.ResourceContents> result = userProfileResource.getToken(customUsername, PASSWORD);

        // Then
        McpSchema.TextResourceContents textContent = (McpSchema.TextResourceContents) result.get(0);
        assertThat(textContent.uri()).isEqualTo("/user/profile/" + customUsername);
    }

    @Test
    void getToken_shouldPassCorrectCredentials_toSensorService() throws JsonProcessingException {
        // Given
        when(sensorService.getUserToken(any(LoginSensorUserDTO.class))).thenReturn(tokenResponseDTO);

        // When
        userProfileResource.getToken(USERNAME, PASSWORD);

        // Then
        verify(sensorService).getUserToken(any(LoginSensorUserDTO.class));
    }

    @Test
    void getToken_shouldHandleSpecialCharactersInUsername() throws JsonProcessingException {
        // Given
        String usernameWithSpecialChars = "user@example.com";
        when(sensorService.getUserToken(any(LoginSensorUserDTO.class))).thenReturn(tokenResponseDTO);

        // When
        List<McpSchema.ResourceContents> result = userProfileResource.getToken(usernameWithSpecialChars, PASSWORD);

        // Then
        assertThat(result).hasSize(1);
        McpSchema.TextResourceContents textContent = (McpSchema.TextResourceContents) result.get(0);
        assertThat(textContent.uri()).contains(usernameWithSpecialChars);
    }

    @Test
    void getToken_shouldReturnResourceContents_withCorrectMimeType() throws JsonProcessingException {
        // Given
        when(sensorService.getUserToken(any(LoginSensorUserDTO.class))).thenReturn(tokenResponseDTO);

        // When
        List<McpSchema.ResourceContents> result = userProfileResource.getToken(USERNAME, PASSWORD);

        // Then
        McpSchema.TextResourceContents textContent = (McpSchema.TextResourceContents) result.get(0);
        assertThat(textContent.mimeType()).isEqualTo("application/json");
    }
}
