package pab.rpg.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

// Bound from openai.* (see application-local.yml/application-cloud.yml/application-test.yml).
@ConfigurationProperties(prefix = "openai")
public record OpenAiProperties(
        boolean enabled,
        String apiKey,
        String baseUrl,
        String model,
        @DefaultValue("2048") int maxOutputTokens,
        @DefaultValue("0.7") double temperature,
        // Optional pricing (USD per token) to estimate cost for the "coste estimado por sesión" metric (TDD §14).
        // Left at 0 unless configured, since prices vary by model/provider and aren't hardcoded here.
        @DefaultValue("0") double costPerInputTokenUsd,
        @DefaultValue("0") double costPerOutputTokenUsd
) {
}
