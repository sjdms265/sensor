package com.sensor.sensorai.controller;

import com.sensor.sensorcommon.dto.HumidexResultDTO;
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
public class HumidexController {

    private final ChatClient chatClient;

    /**
     * Computes the Humidex comfort index for the given user by fetching the latest
     * temperature and humidity readings via the MCP tool and returning a structured
     * {@link HumidexResultDTO} with the numeric index and comfort level.
     *
     * <p>Humidex levels:
     * <ul>
     *   <li>NO_DISCOMFORT — index &lt; 29</li>
     *   <li>SOME_DISCOMFORT — index 30–39</li>
     *   <li>GREAT_DISCOMFORT — index 40–45</li>
     *   <li>DANGEROUS — index 45–54</li>
     *   <li>HEAT_STROKE — index &gt; 54</li>
     * </ul>
     *
     * @param request  the HTTP request (used to extract the JWT token)
     * @param userId   the user whose sensors are queried
     * @return a JSON response containing the Humidex index and comfort level
     */
    @GetMapping("/humidex/{userId}")
    public ResponseEntity<String> getHumidex(HttpServletRequest request,
                                             @PathVariable final String userId) {
        try {
            BeanOutputConverter<HumidexResultDTO> converter = new BeanOutputConverter<>(HumidexResultDTO.class);
            String jsonRepresentation = ChatController.escapeStBraces(converter.getFormat());

            String contents = "Calculate the Humidex comfort index for user {userId}. "
                    + "Use the get-humidex-by-userId-sensorId tool with userId={userId} and token={token}. "
                    + "Return the result as structured JSON. "
                    + ChatController.responseFormat(jsonRepresentation);

            log.info("Humidex request to AI for userId={}", userId);

            String answer = chatClient.prompt()
                    .user(userSpec -> userSpec.text(contents)
                            .param("userId", userId)
                            .param("token", request.getHeader("Authorization")))
                    .call()
                    .content();

            log.info("Humidex answer: {}", answer);
            return ResponseEntity.ok(answer);

        } catch (Exception e) {
            log.error("Error computing Humidex for userId={}: {}", userId, e.getMessage());
            throw new RuntimeException(e);
        }
    }
}
