package com.sensor.sensorai.controller;

import com.sensor.sensorai.service.GraphqlSensorEndpointService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
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
            ChatResponse response = chatModel.call(
                    new Prompt("Who are you? Tell me a dad joke",
                            OllamaOptions.create().withModel(model).withTemperature(temperature)
                    ));

            String answer =  response.getResults().stream().map(generation -> generation.getOutput().getContent()).
                    collect(Collectors.joining());

            String sensorEndpoints = graphqlSensorEndpointService.getSensorEndpoints(userId, sensorId);

            //https://docs.spring.io/spring-ai/reference/api/chat/ollama-chat.html
   /*         var userMessage = new UserMessage("Analyze this json data?",
                    new Media(MimeTypeUtils.APPLICATION_JSON, new ByteArrayResource(sensorEndpoints.getBytes(StandardCharsets.UTF_8))));*/

            String contents = "Analyze this json data and calculate an average temperature: " + sensorEndpoints;
            log.info("request to ai: {}", contents);

            response = chatModel.call(new Prompt(contents, OllamaOptions.builder().withModel(model).withTemperature(temperature)));

            answer += "\n and the json data analysis " + response.getResults().stream().map(generation -> generation.getOutput().getContent()).
                    collect(Collectors.joining());

            return ResponseEntity.ok(answer);
        } catch (Exception e) {
            log.error(e.getMessage());
            throw new RuntimeException(e);
        }

    }

    @GetMapping("/hello")
    public ResponseEntity<String> getChatModel() {
        return getChatModel("sjdms265", "sensor.10000db11e_t");
    }
}
