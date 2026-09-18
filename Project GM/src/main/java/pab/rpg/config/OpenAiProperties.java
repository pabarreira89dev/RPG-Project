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
        @DefaultValue("0.7") double temperature
) {
}
