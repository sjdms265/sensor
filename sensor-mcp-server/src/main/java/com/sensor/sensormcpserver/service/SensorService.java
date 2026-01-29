package com.sensor.sensormcpserver.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sensor.sensormcpserver.dto.GraphSensorEndpoint;
import com.sensor.sensormcpserver.dto.LoginSensorUserDTO;
import com.sensor.sensormcpserver.dto.SensorEndpointDTO;
import com.sensor.sensormcpserver.dto.SensorSpecDTO;
import com.sensor.sensormcpserver.dto.TokenResponseDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

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

    private final RestTemplate restTemplate;

    public List<GraphSensorEndpoint> getSensorEndpointsList(String token, String userId, String sensorId, Integer pageSize){

        //setting up headers
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + token);
        Map<String, Object> requestBody = getStringObjectMap(userId, sensorId, pageSize);

        HttpEntity<Object> entity = new HttpEntity<>(requestBody, headers);

        log.info("calling {} with requestBody: {}", temperatureSensorUrl + GRAPH_QL, requestBody);

        ResponseEntity<String> response = restTemplate.postForEntity(temperatureSensorUrl + GRAPH_QL, entity, String.class);

        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(response.getBody());

            // Navigate to the array - adjust the path based on your actual response
            JsonNode dataNode = root.path("data").path("sensorEndpoints");

            List<GraphSensorEndpoint> graphSensorEndpoints = mapper.treeToValue(dataNode, new TypeReference<>() {
            });

            graphSensorEndpoints.sort(Comparator.comparing(GraphSensorEndpoint::parsedDateTime));

            log.info("response {} with graphSensorEndpoints: {}", temperatureSensorUrl + GRAPH_QL, graphSensorEndpoints);

            return graphSensorEndpoints;
        } catch (Exception e) {
            log.error("Failed to parse GraphSensorEndpoint array", e);
            return new ArrayList<>();
        }

    }

    public List<SensorEndpointDTO> sensorsByUser(String token, String userId){

        //setting up headers
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + token);
        Map<String, Object> requestBody = getSensorByUserObjectMap(userId);

        HttpEntity<Object> entity = new HttpEntity<>(requestBody, headers);

        log.info("calling {} with requestBody: {}", temperatureSensorUrl + GRAPH_QL, requestBody);

        ResponseEntity<String> response = restTemplate.postForEntity(temperatureSensorUrl + GRAPH_QL, entity, String.class);

        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(response.getBody());

            // Navigate to the array - adjust the path based on your actual response
            JsonNode dataNode = root.path("data").path("sensorsByUser");

            List<SensorEndpointDTO> sensorEndpoints = mapper.treeToValue(dataNode, new TypeReference<>() {
            });

            log.info("response {} with sensorsByUser: {}", temperatureSensorUrl + GRAPH_QL, sensorEndpoints);

            return sensorEndpoints;
        } catch (Exception e) {
            log.error("Failed to parse sensorEndpoint array", e);
            return new ArrayList<>();
        }

    }

    public TokenResponseDTO getUserToken(LoginSensorUserDTO loginSensorUserDTO){

        //setting up headers
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Object> entity = new HttpEntity<>(loginSensorUserDTO, headers);

        log.info("calling {} with requestBody: {}", sensorManagerUrl + API_TOKEN, loginSensorUserDTO);

        return restTemplate.postForEntity(sensorManagerUrl + API_TOKEN, entity, TokenResponseDTO.class).getBody();

    }

    public SensorSpecDTO getSensorSpec(String userId, String token){

        //setting up headers
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + token);
        Map<String, Object> requestBody = getSensorByUserObjectMap(userId);

        log.info("calling {} with requestBody: {}", temperatureSensorUrl + SENSOR_SPEC + "/" + userId);

        HttpEntity<Object> entity = new HttpEntity<>(requestBody, headers);

        return restTemplate.exchange(temperatureSensorUrl + SENSOR_SPEC + "/" + userId, HttpMethod.GET, entity,
                SensorSpecDTO.class).getBody();

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

        //create a requestBody with a query
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

        //create a requestBody with a query
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("query", query);
        return requestBody;
    }
}
