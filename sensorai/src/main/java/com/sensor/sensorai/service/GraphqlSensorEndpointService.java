package com.sensor.sensorai.service;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class GraphqlSensorEndpointService {
    
    @Value("${sensor-manager.url}")
    private String url;

    private final RestTemplate restTemplate;

    public String getSensorEndpoints(HttpServletRequest request, String userId, String sensorId, Integer pageSize){

        //setting up headers
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + request.getHeader("Authorization"));

        Map<String, Object> requestBody = getStringObjectMap(userId, sensorId, pageSize);

        HttpEntity<Object> entity = new HttpEntity<>(requestBody, headers);

        ResponseEntity<String> response = restTemplate.postForEntity(url, entity, String.class);

        return response.getBody();
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

        //create requestBody with query
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("query", query);
        return requestBody;
    }
}
