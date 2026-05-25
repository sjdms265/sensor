package com.sensor.sensorai.config;

import com.sensor.sensorai.advisor.AnthropicCacheUsageAdvisor;
import io.modelcontextprotocol.client.McpClient;
import io.modelcontextprotocol.client.McpSyncClient;
import io.modelcontextprotocol.client.transport.HttpClientSseClientTransport;
import io.modelcontextprotocol.spec.McpSchema;
import org.springframework.ai.anthropic.AnthropicChatModel;
import org.springframework.ai.anthropic.AnthropicChatOptions;
import org.springframework.ai.anthropic.api.AnthropicCacheOptions;
import org.springframework.ai.anthropic.api.AnthropicCacheStrategy;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.anthropic.api.AnthropicCacheTtl;
import org.springframework.ai.chat.messages.MessageType;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.mcp.SyncMcpToolCallbackProvider;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;

@Configuration
@Slf4j
public class SensorAiConfig {

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    @Bean
    public AnthropicCacheUsageAdvisor anthropicCacheUsageAdvisor() {
        return new AnthropicCacheUsageAdvisor();
    }

    @Bean(name = "chatClient")
    @ConditionalOnProperty(name = "spring.ai.active-model", havingValue = "ollama")
    public ChatClient ollamaChatClient(OllamaChatModel ollamaChatModel, McpSyncClient mcpSyncClient) {

        var defaultSystem = """
                You are an Backend AI powered assistant that analyze JSON information that contains IOT sensor data with temperature, humidity.
                """;

        return ChatClient.builder(ollamaChatModel).defaultSystem(defaultSystem).defaultToolCallbacks(new SyncMcpToolCallbackProvider(mcpSyncClient)).build();
    }

    @Bean(name = "chatClient")
    @ConditionalOnProperty(name = "spring.ai.active-model", havingValue = "anthropic")
    public ChatClient anthropicChatClient(AnthropicChatModel anthropicChatModel, McpSyncClient mcpSyncClient,
                                          AnthropicCacheUsageAdvisor cacheUsageAdvisor) {

        // This system prompt is intentionally detailed to exceed Anthropic's 1024-token minimum
        // required for prompt caching to activate. The content is cached across requests with
        // SYSTEM_AND_TOOLS strategy, reducing cost and latency on repeated calls.
        var defaultSystem = """
                You are a backend AI-powered assistant that analyzes IoT sensor data collected from physical
                hardware devices. Your role is to interpret sensor readings, compute statistics, detect anomalies,
                and reason about environmental conditions such as rain probability.

                ## Sensor measurements
                Sensors report two primary measurement types:

                **Temperature** — ambient air temperature in degrees Celsius.
                - Typical indoor range: 15°C to 30°C.
                - Typical outdoor range: -20°C to +50°C depending on climate and season.
                - Industrial environments may exceed 60°C near machinery.
                - A sudden spike or drop of more than 10°C within a short window is likely a sensor fault.

                **Humidity** — relative humidity as a percentage (0–100%).
                - Values above 80% indicate high moisture; condensation or rain risk increases.
                - Values below 20% indicate very dry conditions; static electricity and dehydration risk.
                - Sustained humidity above 90% indoors may indicate water ingress or HVAC failure.
                - A reading of exactly 0% or 100% is almost always a sensor fault unless corroborated by multiple devices.

                ## Timestamps
                All sensor readings carry a `parsedDateTime` field in UTC ISO-8601 format
                (e.g. `2025-06-15T14:32:00Z`). This represents the moment the value was recorded by the hardware.
                When performing time-series analysis, treat missing readings as gaps, not zeros.
                Do not interpolate across gaps longer than 30 minutes unless explicitly asked.

                ## Data quality and outlier handling
                Sensor hardware is imperfect. Before computing statistics:
                - Discard values that deviate more than 3 standard deviations from the mean of the dataset.
                - If fewer than 5 readings are available, apply no outlier filtering and note the low sample count.
                - Duplicate timestamps with different values indicate a clock drift issue; use the first reading.
                - Readings with null or negative humidity, or temperature outside -50°C to +80°C, must be discarded.

                ## Statistical computations
                When computing statistics (average, highest, lowest):
                - Apply outlier filtering as described above before computing any aggregate.
                - Round all numeric results to two decimal places.
                - If the dataset is empty after filtering, return null for the statistic and include a note.

                ## Rain probability assessment
                Rain probability is inferred from temperature and humidity trends:
                - High humidity (above 85%) combined with a recent temperature drop (more than 2°C over 30 minutes)
                  strongly correlates with precipitation; assign 70–90% probability.
                - Sustained high humidity (above 90%) with stable temperature: 50–70% probability.
                - Humidity between 70–85% with mild temperature drop: 30–50% probability.
                - Humidity below 70%: below 20% probability unless other factors are present.
                - Express probability as an integer percentage (0–100) with a one-sentence justification.

                ## Output format
                Always respond using the exact JSON schema provided in the user message.
                Do not include any text, markdown, or explanation outside the JSON object.
                All numeric fields must be rounded to two decimal places unless the schema specifies otherwise.
                Null fields must be included as JSON `null`, not omitted.
                """;

        // SYSTEM_AND_TOOLS caches both the system message and MCP tool schemas.
        // Tool schemas are static per request and often large enough to meet Anthropic's 1024-token cache minimum,
        // making them the best candidate for caching in this stateless single-turn request pattern.
        // https://www.youtube.com/watch?v=eYb7BKW4QcU
        AnthropicCacheOptions cacheOptions = AnthropicCacheOptions.builder()
                .strategy(AnthropicCacheStrategy.SYSTEM_AND_TOOLS)
                // Lower Spring AI's own pre-check threshold so it always sends the cache_control breakpoint;
                // Anthropic enforces its own 1024-token minimum server-side independently.
                .messageTypeMinContentLength(MessageType.SYSTEM, 0)
                .build();
        AnthropicChatOptions chatOptions = AnthropicChatOptions.builder().cacheOptions(cacheOptions).build();

        // Wrap the model so every individual API call (including mid-tool-loop calls) logs native usage
        // before Spring AI's tool-calling aggregation can drop the AnthropicApi.Usage native object.
        // Also logs the options class so we can verify AnthropicChatOptions (with cacheOptions) is present.
        ChatModel observedModel = prompt -> {
            log.debug("Calling Anthropic — options type: {}, options value: {}",
                    prompt.getOptions() == null ? "null" : prompt.getOptions().getClass().getSimpleName(),
                    prompt.getOptions());
            var response = anthropicChatModel.call(prompt);
            cacheUsageAdvisor.logUsage(response);
            return response;
        };

        return ChatClient.builder(observedModel).defaultSystem(defaultSystem).defaultOptions(chatOptions)
                .defaultToolCallbacks(new SyncMcpToolCallbackProvider(mcpSyncClient)).build();
    }

    @Bean
    public McpSyncClient mcpSyncClient(@Value("${mcp.sensor-mcp-server.base-url}") String baseUrl) {
        var transport = HttpClientSseClientTransport.builder(baseUrl)
                .sseEndpoint("mcp/sensor/sse")
                .build();

        McpSyncClient client = McpClient.sync(transport)
                .requestTimeout(Duration.ofSeconds(10))
                .clientInfo(new McpSchema.Implementation("MCP Sensor Client", "1.0.0"))
                .build();

        client.initialize();
        return client;
    }

}