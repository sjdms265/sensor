package com.sensor.sensorai.controller;

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

@Controller
@RequestMapping("/sensorai")
@RequiredArgsConstructor
@Slf4j
public class ChatController {

    private final ChatClient ollamaChatClient;

    private final GraphqlSensorEndpointService graphqlSensorEndpointService;

    @GetMapping("/hello/{userId}/{sensorId}")
    public ResponseEntity<String> getChatModel(HttpServletRequest request, final @PathVariable("userId") String userId, final @PathVariable("sensorId") String sensorId) {

        try{

            String sensorEndpoints = graphqlSensorEndpointService.getSensorEndpoints(request, userId, sensorId, 50);

            String contents = "Analyze this json data and calculate the average temperature, highest temperature and lowest temperature: "
                    + sensorEndpoints + "\n" + responseFormat();
            log.info("request to ai: {}", contents);

            String answer =  ollamaChatClient.prompt().user(contents).call().content();

            log.info("answer: {}", answer);

            return ResponseEntity.ok(answer);
        } catch (Exception e) {
            log.error(e.getMessage());
            throw new RuntimeException(e);
        }

    }

    //https://www.baeldung.com/spring-artificial-intelligence-structure-output
    public static String responseFormat() {

        BeanOutputConverter<TemperatureResults> beanOutputConverter = new BeanOutputConverter<>(TemperatureResults.class);

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

    @GetMapping("/hello")
    public ResponseEntity<String> getChatModel(HttpServletRequest request) {
        return getChatModel(request, "sjdms265", "sensor.10000db11e_t");
    }
}
