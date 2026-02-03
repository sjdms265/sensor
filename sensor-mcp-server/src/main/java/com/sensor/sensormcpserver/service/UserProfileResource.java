package com.sensor.sensormcpserver.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sensor.sensorcommon.dto.LoginSensorUserDTO;
import com.sensor.sensormcpserver.dto.TokenResponseDTO;
import io.modelcontextprotocol.spec.McpSchema;
import lombok.RequiredArgsConstructor;
import org.springaicommunity.mcp.annotation.McpResource;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
@Service
public class UserProfileResource {

    private final SensorService sensorService;

    // This method is exposed as an MCP resource with the URI pattern "/user/profile/{userId}"
    @McpResource(uri = "/user/profile/{username}/{password}")
    public List<McpSchema.ResourceContents> getToken(String username, String password) throws JsonProcessingException {

        LoginSensorUserDTO loginSensorUserDTO = new LoginSensorUserDTO(username, password);
        TokenResponseDTO tokenResponseDTO = sensorService.getUserToken(loginSensorUserDTO);

        ObjectMapper objectMapper = new ObjectMapper();

        List<McpSchema.ResourceContents> resourceContents = new ArrayList<>();
        McpSchema.ResourceContents resourceContent = new McpSchema.TextResourceContents("/user/profile/" + username,
                "application/json", objectMapper.writeValueAsString(tokenResponseDTO));
        resourceContents.add(resourceContent);

        return resourceContents;


    }
}

