package com.sensor.sensormcpserver.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.sensor.sensormcpserver.dto.TemperatureResults;
import io.modelcontextprotocol.spec.McpSchema;
import org.springaicommunity.mcp.annotation.McpArg;
import org.springaicommunity.mcp.annotation.McpPrompt;
import org.springframework.ai.converter.BeanOutputConverter;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SensorPrompts {

    @McpPrompt(
            name = "sensor_stats",
            description = "Get basic stats for a sensor id"
    )
    public McpSchema.GetPromptResult generateStatsPrompt(
            @McpArg(name = "userId", description = "The userId looked up when filtering", required = true) String userId,
            @McpArg(name = "sensorId", description = "The sensorId looked up when filtering", required = true) String sensorId) {

        BeanOutputConverter<TemperatureResults> beanOutputTemperatureConverter = new BeanOutputConverter<>(TemperatureResults.class);
        String jsonRepresentation = escapeStBraces(beanOutputTemperatureConverter.getFormat());

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        // By default, Jackson writes dates as timestamps (numbers). Turn that off
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        String contents = "Calculate average value, highest value and lowest value for the user %s? and sensor %s, use {token} "
                + responseFormat(jsonRepresentation);

        String content = String.format(contents, userId, sensorId);

        return new McpSchema.GetPromptResult(
                "Sensor Stats",
                List.of(new McpSchema.PromptMessage(McpSchema.Role.USER, new McpSchema.TextContent(content)))
        );
    }

    public static String responseFormat(String jsonRepresentation) {

        String template = """
                %s.
                the json property parsedDateTime is the timestamp when the temperature was recorded and it is UTC formated.
                """;
        return String.format(template, jsonRepresentation);
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
