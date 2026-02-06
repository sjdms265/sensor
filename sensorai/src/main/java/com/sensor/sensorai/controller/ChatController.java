package com.sensor.sensorai.controller;

import com.sensor.sensorai.dto.Rain;
import com.sensor.sensorai.dto.SensorStatsResults;
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

    private final ChatClient chatClient;

    @GetMapping("/stats/{userId}/{sensorId}")
    public ResponseEntity<String> getBasicStats(HttpServletRequest request, @PathVariable final String userId,
                                                final @PathVariable String sensorId) {

        try{

            BeanOutputConverter<SensorStatsResults> beanOutputConverter = new BeanOutputConverter<>(SensorStatsResults.class);
            String jsonRepresentation = escapeStBraces(beanOutputConverter.getFormat());

            String contents = "Calculate average value, highest value and lowest value for the user {userId} and sensor {sensorId}. Use a list size of {pageSize} SensorEndpoints and use {token} "
                    + responseFormat(jsonRepresentation);
            log.info("request to ai: {}", contents);

            String answer =  chatClient.prompt().user(contents).user(userSpec -> userSpec.text(contents).param("userId", userId).
                    param("sensorId", sensorId).param("pageSize", 50).param("token", request.getHeader("Authorization"))).call().content();

            log.info("answer: {}", answer);

            return ResponseEntity.ok(answer);
        } catch (Exception e) {
            log.error(e.getMessage());
            throw new RuntimeException(e);
        }

    }

    @GetMapping("/rain/{userId}")
    public ResponseEntity<String> rainProbability(HttpServletRequest request, @PathVariable final String userId) {

        try{

            BeanOutputConverter<Rain> beanOutputConverter = new BeanOutputConverter<>(Rain.class);
            String jsonRepresentation = escapeStBraces(beanOutputConverter.getFormat());

            String contents = "What is the probability of rain for today for the user {userId}?. The last values of temperature and humidity are {pattern}, use {token} "
                    +  responseFormat(jsonRepresentation);
            log.info("request to ai: {}", contents);

            String answer =  chatClient.prompt().user(userSpec -> userSpec.text(contents).param("userId", userId).
                    param("pattern", "temperature,humidity").param("token", request.getHeader("Authorization"))).call().content();

            log.info("answer: {}", answer);

            return ResponseEntity.ok(answer);
        } catch (Exception e) {
            log.error(e.getMessage());
            throw new RuntimeException(e);
        }

    }

    //https://www.baeldung.com/spring-artificial-intelligence-structure-output
    public static String responseFormat(String jsonRepresentation) {

        String template = """
                %s.
                the json property parsedDateTime is the timestamp when the temperature was recorded and it is UTC formated.
                """;
        return String.format(template, jsonRepresentation);
    }

    @GetMapping("/stats/hello")
    public ResponseEntity<String> getChatModel(HttpServletRequest request) {
        return getBasicStats(request, "sjdms265", "sensor.10000db11e_t");
    }

    /**
            * Spring AI's ST (StringTemplate) renderer treats { ... } as template syntax.
            * JSON/JSON-Schema contains lots of braces, so we must escape them when passing as parameters.
     */
    private static String escapeStBraces(String input) {
        if (input == null) return null;
        return input
                .replace("\\", "\\\\")
                .replace("{", "\\{")
                .replace("}", "\\}");
    }
}
