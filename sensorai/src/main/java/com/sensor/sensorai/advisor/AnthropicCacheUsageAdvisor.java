package com.sensor.sensorai.advisor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.anthropic.api.AnthropicApi;
import org.springframework.ai.chat.model.ChatResponse;

@Slf4j
public class AnthropicCacheUsageAdvisor {

    public void logUsage(ChatResponse response) {
        if (response == null) return;

        var usage = response.getMetadata().getUsage();
        if (usage == null) return;

        // AnthropicChatModel in Spring AI 1.1.2 passes AnthropicApi.Usage as the native usage object
        // (confirmed via bytecode analysis of spring-ai-anthropic-1.1.2.jar).
        if (usage.getNativeUsage() instanceof AnthropicApi.Usage anthropicUsage) {
            log.info("Anthropic usage — input: {}, output: {}, cache_creation: {}, cache_read: {}",
                    anthropicUsage.inputTokens(),
                    anthropicUsage.outputTokens(),
                    anthropicUsage.cacheCreationInputTokens(),
                    anthropicUsage.cacheReadInputTokens());
        } else {
            // Fallback: log standard usage if native object is unexpectedly absent.
            log.info("usage — prompt tokens: {}, completion tokens: {}, total: {}",
                    usage.getPromptTokens(),
                    usage.getCompletionTokens(),
                    usage.getTotalTokens());
        }
    }
}
