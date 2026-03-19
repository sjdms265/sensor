package com.sensor.sensorai.advisor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.anthropic.api.AnthropicApi;
import org.springframework.ai.chat.model.ChatResponse;

@Slf4j
public class AnthropicCacheUsageAdvisor {

    public void logUsage(ChatResponse response) {
        if (response == null || response.getMetadata() == null) return;

        var usage = response.getMetadata().getUsage();
        if (usage == null) return;

        if (usage.getNativeUsage() instanceof AnthropicApi.Usage anthropicUsage) {
            log.info("Anthropic usage — input: {}, output: {}, cache_creation: {}, cache_read: {}",
                    anthropicUsage.inputTokens(),
                    anthropicUsage.outputTokens(),
                    anthropicUsage.cacheCreationInputTokens(),
                    anthropicUsage.cacheReadInputTokens());
        } else {
            log.warn("Native Anthropic usage not available (class={})",
                    usage.getNativeUsage() == null ? "null" : usage.getNativeUsage().getClass().getName());
        }
    }
}