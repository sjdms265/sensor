package com.sensor.sensormcpserver.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sensor.sensormcpserver.dto.GraphSensorEndpoint;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
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

    @Value("${temperature-sensor.url}")
    private String url;

    private final RestTemplate restTemplate;

    public List<GraphSensorEndpoint> getSensorEndpointsList(String token, String userId, String sensorId, Integer pageSize){

        //setting up headers
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + token);
        Map<String, Object> requestBody = getStringObjectMap(userId, sensorId, pageSize);

        HttpEntity<Object> entity = new HttpEntity<>(requestBody, headers);

        log.info("calling {} with requestBody: {}", url, requestBody);

        ResponseEntity<String> response = restTemplate.postForEntity(url, entity, String.class);

        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(response.getBody());

            // Navigate to the array - adjust the path based on your actual response
            JsonNode dataNode = root.path("data").path("sensorEndpoints");

            List<GraphSensorEndpoint> graphSensorEndpoints = mapper.treeToValue(dataNode, new TypeReference<>() {
            });

            graphSensorEndpoints.sort(Comparator.comparing(GraphSensorEndpoint::parsedDateTime));

            log.info("response {} with graphSensorEndpoints: {}", url, graphSensorEndpoints);

            return graphSensorEndpoints;
        } catch (Exception e) {
            log.error("Failed to parse GraphSensorEndpoint array", e);
            return new ArrayList<>();
        }

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
}
