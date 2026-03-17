package com.sensor.sensormcpserver.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sensor.sensorcommon.dto.GraphSensorEndpoint;
import com.sensor.sensorcommon.dto.LoginSensorUserDTO;
import com.sensor.sensorcommon.dto.SensorEndpointDTO;
import com.sensor.sensorcommon.dto.SensorSpecDTO;
import com.sensor.sensormcpserver.dto.TokenResponseDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class SensorService {

    public static final String API_TOKEN = "/api/auth/token";
    public static final String SENSOR_SPEC = "/sensorSpecs";
    public static final String GRAPH_QL = "/graphql";

    @Value("${temperature-sensor.url}")
    private String temperatureSensorUrl;

    @Value("${sensor-manager.url}")
    private String sensorManagerUrl;

    private final WebClient webClient;
    private final ObjectMapper objectMapper;

    public List<GraphSensorEndpoint> getSensorEndpointsList(String token, String userId, String sensorId, Integer pageSize) {
        Map<String, Object> requestBody = getStringObjectMap(userId, sensorId, pageSize);
        String url = temperatureSensorUrl + GRAPH_QL;

        log.info("calling {} with requestBody: {}", url, requestBody);

        String responseBody = webClient.post()
                .uri(url)
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + token)
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(String.class)
                .block();

        try {
            JsonNode root = objectMapper.readTree(responseBody);
            JsonNode dataNode = root.path("data").path("sensorEndpoints");

            List<GraphSensorEndpoint> graphSensorEndpoints = objectMapper.treeToValue(dataNode, new TypeReference<>() {});
            graphSensorEndpoints.sort(Comparator.comparing(GraphSensorEndpoint::parsedDateTime));

            log.info("response {} with graphSensorEndpoints: {}", url, graphSensorEndpoints);
            return graphSensorEndpoints;
        } catch (Exception e) {
            log.error("Failed to parse GraphSensorEndpoint array", e);
            return new ArrayList<>();
        }
    }

    public List<SensorEndpointDTO> sensorsByUser(String token, String userId) {
        Map<String, Object> requestBody = getSensorByUserObjectMap(userId);
        String url = temperatureSensorUrl + GRAPH_QL;

        log.info("calling {} with requestBody: {}", url, requestBody);

        String responseBody = webClient.post()
                .uri(url)
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + token)
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(String.class)
                .block();

        try {
            JsonNode root = objectMapper.readTree(responseBody);
            JsonNode dataNode = root.path("data").path("sensorsByUser");

            List<SensorEndpointDTO> sensorEndpoints = objectMapper.treeToValue(dataNode, new TypeReference<>() {});
            if (sensorEndpoints == null) {
                sensorEndpoints = new ArrayList<>();
            }

            log.info("response {} with sensorsByUser: {}", url, sensorEndpoints);
            return sensorEndpoints;
        } catch (Exception e) {
            log.error("Failed to parse sensorEndpoint array", e);
            return new ArrayList<>();
        }
    }

    public TokenResponseDTO getUserToken(LoginSensorUserDTO loginSensorUserDTO) {
        String url = sensorManagerUrl + API_TOKEN;

        log.info("calling {} with requestBody: {}", url, loginSensorUserDTO);

        return webClient.post()
                .uri(url)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(loginSensorUserDTO)
                .retrieve()
                .bodyToMono(TokenResponseDTO.class)
                .block();
    }

    public SensorSpecDTO getSensorSpec(String sensorId, String token) {
        String url = temperatureSensorUrl + SENSOR_SPEC + "/" + sensorId;

        log.info("calling {}", url);

        return webClient.get()
                .uri(url)
                .header("Authorization", "Bearer " + token)
                .retrieve()
                .bodyToMono(SensorSpecDTO.class)
                .block();
    }

    private static Map<String, Object> getStringObjectMap(String userId, String sensorId, Integer pageSize) {
        String query = """
                query {
                	sensorEndpoints(userId : "$userId", sensorId : "$sensorId", pageSize : $pageSize, pageNumber: 0) {
                    parsedDateTime,
                    value
                  }
                }
                """.replace("$userId", userId).replace("$sensorId", sensorId).replace("$pageSize", pageSize.toString());

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("query", query);
        return requestBody;
    }

    private static Map<String, Object> getSensorByUserObjectMap(String userId) {
        String query = """
                query {
                	sensorsByUser(userId : "$userId") {
                    sensorId
                  }
                }
                """.replace("$userId", userId);

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("query", query);
        return requestBody;
    }
}
