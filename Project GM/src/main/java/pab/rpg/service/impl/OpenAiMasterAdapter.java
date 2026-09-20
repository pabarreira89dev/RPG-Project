package pab.rpg.service.impl;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import pab.rpg.config.OpenAiProperties;
import pab.rpg.domain.rules.ActionType;
import pab.rpg.exception.AiUnavailableException;
import pab.rpg.service.MasterAdapter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

// Only component aware of the OpenAI HTTP protocol (Responses API); the rest of the app depends on MasterAdapter.
// Connect/read timeouts come from spring.http.client.* (application-cloud.yml), applied by Spring Boot's
// auto-configured RestClient.Builder, so they still take effect and don't clash with MockRestServiceServer in tests.
@Service
@ConditionalOnProperty(prefix = "openai", name = "enabled", havingValue = "true")
public class OpenAiMasterAdapter implements MasterAdapter {

    private static final Logger LOG = LoggerFactory.getLogger(OpenAiMasterAdapter.class);

    private static final String NARRATION_INSTRUCTIONS = """
            Eres el narrador de un RPG de fantasía. Narra en español, en un párrafo breve, el resultado \
            que el motor del juego ya ha decidido. No inventes hechos, objetos ni daño, y no cambies el resultado indicado.""";

    private static final String INTERPRETATION_INSTRUCTIONS = """
            Clasifica la intención del jugador de un RPG de fantasía. Devuelve exactamente un actionType de \
            EXPLORATION, SOCIAL, INVESTIGATION o PHYSICAL. Si el texto señala claramente a uno de los NPCs de la \
            lista proporcionada como objetivo de la acción, devuelve su id exacto en targetNpcId; si no hay un \
            objetivo claro entre esos NPCs, devuelve null. Nunca inventes un id que no esté en la lista.""";

    private static final String CANDIDATE_SELECTION_INSTRUCTIONS = """
            Elige, entre las opciones proporcionadas, la que mejor corresponda a la intención del jugador. \
            Devuelve exactamente el id de una opción de la lista en candidateId, o null si ninguna corresponde \
            claramente. Nunca inventes un id que no esté en la lista.""";

    private static final Map<String, Object> ACTION_INTENT_SCHEMA = Map.of(
            "type", "object",
            "properties", Map.of(
                    "actionType", Map.of(
                            "type", "string",
                            "enum", List.of("EXPLORATION", "SOCIAL", "INVESTIGATION", "PHYSICAL")
                    ),
                    "targetNpcId", Map.of(
                            "type", List.of("string", "null")
                    )
            ),
            "required", List.of("actionType", "targetNpcId"),
            "additionalProperties", false
    );

    private final RestClient restClient;
    private final OpenAiProperties properties;
    private final ObjectMapper objectMapper;
    private final MeterRegistry meterRegistry;

    public OpenAiMasterAdapter(OpenAiProperties properties, RestClient.Builder restClientBuilder, ObjectMapper objectMapper,
                               MeterRegistry meterRegistry) {
        this.properties = properties;
        this.objectMapper = objectMapper;
        this.meterRegistry = meterRegistry;
        this.restClient = restClientBuilder
                .baseUrl(properties.baseUrl())
                .defaultHeader("Authorization", "Bearer " + properties.apiKey())
                .build();
    }

    @Override
    public String narrate(NarrationRequest request) {
        Timer.Sample sample = Timer.start(meterRegistry);
        boolean success = false;
        try {
            OpenAiResponse response = restClient.post()
                    .uri("/responses")
                    .body(Map.of(
                            "model", properties.model(),
                            "max_output_tokens", properties.maxOutputTokens(),
                            "temperature", properties.temperature(),
                            "instructions", NARRATION_INSTRUCTIONS,
                            "input", buildPrompt(request)
                    ))
                    .retrieve()
                    .body(OpenAiResponse.class);
            recordUsageMetrics("narrate", response);

            String narration = extractOutputText(response);
            if (narration == null || narration.isBlank()) {
                // Diagnóstico sin volcar el prompt/la respuesta completa (pueden contener datos de la partida).
                LOG.warn("OpenAI respondió sin narración utilizable (grade={})", request.grade());
                throw new AiUnavailableException("OpenAI no devolvió narración.");
            }
            success = true;
            return narration;
        } catch (RestClientException exception) {
            LOG.warn("Fallo al contactar con OpenAI: {}", exception.getMessage());
            throw new AiUnavailableException("No se pudo contactar con OpenAI: " + exception.getMessage());
        } finally {
            recordCallMetrics("narrate", sample, success);
        }
    }

    @Override
    public ActionIntent interpret(InterpretationRequest request) {
        Timer.Sample sample = Timer.start(meterRegistry);

        LOG.info("Interpretando intención del jugador: sceneSummary='{}', playerText='{}', visibleNpcs={}",
                request.sceneSummary(),
                request.playerText(),
                request.visibleNpcs().stream()
                        .map(npc -> npc.id() + ":" + npc.name())
                        .collect(Collectors.joining(", "))
        );

        boolean success = false;
        try {
            OpenAiResponse response = restClient.post()
                    .uri("/responses")
                    .body(Map.of(
                            "model", properties.model(),
                            "instructions", INTERPRETATION_INSTRUCTIONS,
                            "input", buildInterpretationPrompt(request),
                            "text", Map.of("format", Map.of(
                                    "type", "json_schema",
                                    "name", "action_intent",
                                    "strict", true,
                                    "schema", ACTION_INTENT_SCHEMA
                            ))
                    ))
                    .retrieve()
                    .body(OpenAiResponse.class);
            recordUsageMetrics("interpret", response);

            String json = extractOutputText(response);
            if (json == null || json.isBlank()) {
                LOG.warn("OpenAI no devolvió una interpretación utilizable.");
                throw new AiUnavailableException("OpenAI no devolvió una interpretación utilizable.");
            }

            LOG.info("OpenAI interpretó la intención del jugador como: {}", json);

            RawActionIntent raw = objectMapper.readValue(json, RawActionIntent.class);
            ActionIntent intent = new ActionIntent(ActionType.valueOf(raw.actionType()), parseNpcId(raw.targetNpcId()));
            success = true;
            return intent;
        } catch (RestClientException exception) {
            LOG.warn("Fallo al contactar con OpenAI: {}", exception.getMessage());
            throw new AiUnavailableException("No se pudo contactar con OpenAI: " + exception.getMessage());
        } catch (JsonProcessingException | IllegalArgumentException exception) {
            // JSON inválido o actionType desconocido: se rechaza sin mutar estado (TDD sección 10).
            LOG.warn("OpenAI devolvió una interpretación inválida: {}", exception.getMessage());
            throw new AiUnavailableException("OpenAI devolvió una interpretación inválida.");
        } finally {
            recordCallMetrics("interpret", sample, success);
        }
    }

    // A hallucinated/non-UUID target is simply dropped here; a real-but-invalid NPC id still gets
    // rejected downstream by NpcTargetRule, which is the single source of truth for that check.
    private UUID parseNpcId(String rawId) {
        if (rawId == null || rawId.isBlank()) {
            return null;
        }
        try {
            return UUID.fromString(rawId);
        } catch (IllegalArgumentException exception) {
            return null;
        }
    }

    @Override
    public String selectCandidate(CandidateSelectionRequest request) {
        Timer.Sample sample = Timer.start(meterRegistry);
        boolean success = false;
        try {
            OpenAiResponse response = restClient.post()
                    .uri("/responses")
                    .body(Map.of(
                            "model", properties.model(),
                            "instructions", CANDIDATE_SELECTION_INSTRUCTIONS,
                            "input", buildCandidateSelectionPrompt(request),
                            "text", Map.of("format", Map.of(
                                    "type", "json_schema",
                                    "name", "candidate_selection",
                                    "strict", true,
                                    "schema", buildCandidateSelectionSchema(request.candidates())
                            ))
                    ))
                    .retrieve()
                    .body(OpenAiResponse.class);
            recordUsageMetrics("selectCandidate", response);

            String json = extractOutputText(response);
            if (json == null || json.isBlank()) {
                LOG.warn("OpenAI no devolvió una selección utilizable.");
                throw new AiUnavailableException("OpenAI no devolvió una selección utilizable.");
            }

            RawCandidateSelection raw = objectMapper.readValue(json, RawCandidateSelection.class);
            success = true;
            return raw.candidateId();
        } catch (RestClientException exception) {
            LOG.warn("Fallo al contactar con OpenAI: {}", exception.getMessage());
            throw new AiUnavailableException("No se pudo contactar con OpenAI: " + exception.getMessage());
        } catch (JsonProcessingException exception) {
            LOG.warn("OpenAI devolvió una selección inválida: {}", exception.getMessage());
            throw new AiUnavailableException("OpenAI devolvió una selección inválida.");
        } finally {
            recordCallMetrics("selectCandidate", sample, success);
        }
    }

    // Builds the enum dynamically from the actual candidates of this call (stronger than a static enum:
    // the model can only pick a real id from this exact request, or null).
    private Map<String, Object> buildCandidateSelectionSchema(List<Candidate> candidates) {
        List<Object> allowedIds = new ArrayList<>(candidates.stream().map(Candidate::id).toList());
        allowedIds.add(null);

        Map<String, Object> candidateIdSchema = Map.of(
                "type", List.of("string", "null"),
                "enum", allowedIds
        );

        return Map.of(
                "type", "object",
                "properties", Map.of("candidateId", candidateIdSchema),
                "required", List.of("candidateId"),
                "additionalProperties", false
        );
    }

    private String buildCandidateSelectionPrompt(CandidateSelectionRequest request) {
        String options = request.candidates().stream()
                .map(candidate -> candidate.id() + ": " + candidate.label())
                .collect(Collectors.joining("\n"));
        return """
                Opciones disponibles (id: descripción):
                %s
                Texto del jugador: %s""".formatted(options, request.playerText());
    }

    private String buildPrompt(NarrationRequest request) {
        return """
                Escena: %s
                Acción del jugador: %s
                Resultado del motor: %s
                Eventos: %s""".formatted(request.sceneSummary(), request.actionText(), request.grade(), request.eventsSummary());
    }

    private String buildInterpretationPrompt(InterpretationRequest request) {
        String npcList = request.visibleNpcs().stream()
                .map(npc -> npc.id() + ": " + npc.name())
                .collect(Collectors.joining("\n"));
        return """
                Escena: %s
                NPCs visibles (id: nombre):
                %s
                Texto del jugador: %s""".formatted(request.sceneSummary(), npcList, request.playerText());
    }

    // The Responses API returns text nested in output[].content[]; there is no flat "outputText"
    // field outside the official SDKs, so it has to be extracted manually here.
    private String extractOutputText(OpenAiResponse response) {
        if (response == null || response.output() == null) {
            return null;
        }
        return response.output().stream()
                .filter(item -> "message".equals(item.type()) && item.content() != null)
                .flatMap(item -> item.content().stream())
                .filter(content -> "output_text".equals(content.type()))
                .map(OutputContent::text)
                .findFirst()
                .orElse(null);
    }

    // TDD §14 metrics: duration/errors per operation and input/output token counts.
    private void recordCallMetrics(String operation, Timer.Sample sample, boolean success) {
        sample.stop(meterRegistry.timer("pab.rpg.openai.duration", "operation", operation,
                "outcome", success ? "success" : "error"));
        if (!success) {
            meterRegistry.counter("pab.rpg.openai.errors", "operation", operation).increment();
        }
    }

    // sessionId is already in MDC (CorrelationIdFilter) for any request-bound call, so this log line
    // is attributable per session without tagging Micrometer metrics with an unbounded sessionId.
    private void recordUsageMetrics(String operation, OpenAiResponse response) {
        if (response == null || response.usage() == null) {
            return;
        }
        int inputTokens = response.usage().inputTokens();
        int outputTokens = response.usage().outputTokens();
        meterRegistry.counter("pab.rpg.openai.tokens", "operation", operation, "direction", "input").increment(inputTokens);
        meterRegistry.counter("pab.rpg.openai.tokens", "operation", operation, "direction", "output").increment(outputTokens);

        double estimatedCostUsd = inputTokens * properties.costPerInputTokenUsd() + outputTokens * properties.costPerOutputTokenUsd();
        if (estimatedCostUsd > 0) {
            meterRegistry.counter("pab.rpg.openai.estimated.cost.usd").increment(estimatedCostUsd);
            LOG.info("openai_cost_estimate operation={} inputTokens={} outputTokens={} estimatedCostUsd={}",
                    operation, inputTokens, outputTokens, estimatedCostUsd);
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record OpenAiResponse(List<OutputItem> output, Usage usage) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record Usage(
            @JsonProperty("input_tokens") int inputTokens,
            @JsonProperty("output_tokens") int outputTokens
    ) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record OutputItem(String type, List<OutputContent> content) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record OutputContent(String type, String text) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record RawActionIntent(String actionType, String targetNpcId) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record RawCandidateSelection(String candidateId) {
    }
}
