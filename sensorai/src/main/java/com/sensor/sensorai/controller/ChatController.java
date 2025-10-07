package com.sensor.sensorai.controller;

import com.sensor.sensorai.dto.GraphSensorEndpoint;
import com.sensor.sensorai.dto.Rain;
import com.sensor.sensorai.dto.TemperatureResults;
import com.sensor.sensorai.service.GraphqlSensorEndpointService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.converter.BeanOutputConverter;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequestMapping("/sensorai")
@RequiredArgsConstructor
@Slf4j
public class ChatController {

    private final ChatClient chatClient;

    private final GraphqlSensorEndpointService graphqlSensorEndpointService;

    @GetMapping("/stats/{userId}/{sensorId}/{pageSize}")
    public ResponseEntity<String> getBasicStats(HttpServletRequest request, final @PathVariable("userId") String userId, final @PathVariable("sensorId") String sensorId, @PathVariable(required = false) Integer pageSize) {

        if(pageSize == null) pageSize = 10;

        try{

            String sensorEndpoints = graphqlSensorEndpointService.getSensorEndpoints(request, userId, sensorId, pageSize);

            String contents = "Analyze this json data and calculate the average temperature, highest temperature and lowest temperature: "
                    + sensorEndpoints + "\n" + responseFormat("TemperatureResults");
            log.info("request to ai: {}", contents);

            String answer =  chatClient.prompt().user(contents).call().content();

            log.info("answer: {}", answer);

            return ResponseEntity.ok(answer);
        } catch (Exception e) {
            log.error(e.getMessage());
            throw new RuntimeException(e);
        }

    }

    @GetMapping("/graphline/{userId}/{sensorId}")
    public ResponseEntity<List<GraphSensorEndpoint>> getGraphline(HttpServletRequest request, final @PathVariable("userId") String userId, final @PathVariable("sensorId") String sensorId) {

        try{

            List<GraphSensorEndpoint> graphSensorEndpoints = graphqlSensorEndpointService.getSensorEndpointsList(request, userId, sensorId, 50);
            log.info("answer: {}", graphSensorEndpoints);

            return ResponseEntity.ok(graphSensorEndpoints);
        } catch (Exception e) {
            log.error(e.getMessage());
            throw new RuntimeException(e);
        }

    }

    @GetMapping("/rain/{userId}/{pageSize}")
    public ResponseEntity<String> rainProbability(HttpServletRequest request, final @PathVariable("userId") String userId, @PathVariable(required = false) Integer pageSize) {

        if(pageSize == null) pageSize = 10;

        try{

            //FIXME endpoint that get en sensor associated to an user
            String sensorEndpointsTemperature = graphqlSensorEndpointService.getSensorEndpoints(request, userId, "sensor.10000db11e_t", pageSize);
            String sensorEndpointsHumidity = graphqlSensorEndpointService.getSensorEndpoints(request, userId, "sensor.10000db11e_h", pageSize);

            String contents = "What is the probability of rain for today? The last values of temperature are " +
                    sensorEndpointsTemperature + " and the last values of temperature are" + sensorEndpointsHumidity +  responseFormat("Rain");
            log.info("request to ai: {}", contents);

            String answer =  chatClient.prompt().user(contents).call().content();

            log.info("answer: {}", answer);

            return ResponseEntity.ok(answer);
        } catch (Exception e) {
            log.error(e.getMessage());
            throw new RuntimeException(e);
        }

    }

    //https://www.baeldung.com/spring-artificial-intelligence-structure-output
    public static String responseFormat(String className) {

        BeanOutputConverter beanOutputConverter = null;

        //FIXME repace by factory
        switch (className) {
            case "TemperatureResults":  beanOutputConverter = new BeanOutputConverter<>(TemperatureResults.class);
            case "Rain" :  beanOutputConverter = new BeanOutputConverter<>(Rain.class);
        }

        String format = beanOutputConverter.getFormat();

        String template = """
                Your response should be in JSON format.
                Do not include any explanations, only provide a RFC8259 compliant JSON response following this format without deviation.
                Do not include markdown code blocks in your response.
                Remove the ```json markdown from the output.
                the json property parsedDateTime is the timestamp when the temperature was recorded and it is UTC formated.
                the json property value is the value of the temperature in celsius when the temperature was recorded.
                Here is the JSON Schema instance your output must adhere to:
                ```%s```
                """;
        return String.format(template, format);

    }

    @GetMapping("/stats/hello")
    public ResponseEntity<String> getChatModel(HttpServletRequest request) {
        return getBasicStats(request, "sjdms265", "sensor.10000db11e_t", null);
    }
}
