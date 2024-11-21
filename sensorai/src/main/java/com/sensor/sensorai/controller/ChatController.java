package com.sensor.sensorai.controller;

import com.sensor.sensorai.TemperatureResults;
import com.sensor.sensorai.service.GraphqlSensorEndpointService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.converter.BeanOutputConverter;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.ai.ollama.api.OllamaOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.stream.Collectors;

@Controller
@RequestMapping("/sensorai")
@RequiredArgsConstructor
@Slf4j
public class ChatController {

    @Value("${spring.ai.ollama.chat.options.model}")
    private String model;

    @Value("${spring.ai.ollama.chat.options.temperature}")
    private Double temperature;

    private final OllamaChatModel chatModel;

    private final GraphqlSensorEndpointService graphqlSensorEndpointService;

    @GetMapping("/hello/{userId}/{sensorId}")
    public ResponseEntity<String> getChatModel(final @PathVariable("userId") String userId, final @PathVariable("sensorId") String sensorId) {

        try{

            String sensorEndpoints = graphqlSensorEndpointService.getSensorEndpoints(userId, sensorId);

            //https://docs.spring.io/spring-ai/reference/api/chat/ollama-chat.html
   /*         var userMessage = new UserMessage("Analyze this json data?",
                    new Media(MimeTypeUtils.APPLICATION_JSON, new ByteArrayResource(sensorEndpoints.getBytes(StandardCharsets.UTF_8))));*/

            String contents = "Analyze this json data and calculate the average temperature, highest temperature and lowest temperature: "
                    + sensorEndpoints + "\n" + responseFormat();
            log.info("request to ai: {}", contents);

            ChatResponse response = chatModel.call(new Prompt(contents, OllamaOptions.builder().withModel(model).withTemperature(temperature)));

            String answer = "[" + response.getResults().stream().
                    map(generation -> "{" +generation.getOutput().getContent() + "}").
                    collect(Collectors.joining(",")) + "]";

            return ResponseEntity.ok(answer);
        } catch (Exception e) {
            log.error(e.getMessage());
            throw new RuntimeException(e);
        }

    }

    //https://www.baeldung.com/spring-artificial-intelligence-structure-output
    private String responseFormat() {

        BeanOutputConverter<TemperatureResults> beanOutputConverter = new BeanOutputConverter<>(TemperatureResults.class);

        String format = beanOutputConverter.getFormat();

        String template = "Your response should be in JSON format.\n"
                + "Do not include any explanations, only provide a RFC8259 compliant JSON response following this format without deviation.\n"
                + "Do not include markdown code blocks in your response.\n"
                + "Remove the ```json markdown from the output.\n"
                + "the json property parsedDateTime is the timestamp when the temperature was recorded and it is UTC formated.\n"
                + "the json property value is the value of the temperature in celsius when the temperature was recorded."
                + "Here is the JSON Schema instance your output must adhere to:\n```%s```\n";
        return String.format(template, format);

    }

    @GetMapping("/hello")
    public ResponseEntity<String> getChatModel() {
        return getChatModel("sjdms265", "sensor.10000db11e_t");
    }
}
