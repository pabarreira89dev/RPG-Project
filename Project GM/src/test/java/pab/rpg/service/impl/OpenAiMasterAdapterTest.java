package pab.rpg.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;
import pab.rpg.config.OpenAiProperties;
import pab.rpg.domain.rules.ActionType;
import pab.rpg.domain.rules.ResultGrade;
import pab.rpg.exception.AiUnavailableException;
import pab.rpg.service.MasterAdapter.ActionIntent;
import pab.rpg.service.MasterAdapter.Candidate;
import pab.rpg.service.MasterAdapter.CandidateSelectionRequest;
import pab.rpg.service.MasterAdapter.InterpretationRequest;
import pab.rpg.service.MasterAdapter.NarrationRequest;
import pab.rpg.service.MasterAdapter.VisibleNpc;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class OpenAiMasterAdapterTest {

    private final OpenAiProperties properties = new OpenAiProperties(
            true, "test-key", "https://fake-openai.test/v1", "gpt-test", 512, 0.5, 0, 0
    );
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final SimpleMeterRegistry meterRegistry = new SimpleMeterRegistry();

    @Test
    void narrateExtractsTextFromResponsesApiOutput() {
        RestClient.Builder builder = RestClient.builder();
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        server.expect(requestTo("https://fake-openai.test/v1/responses"))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withSuccess("""
                        {
                          "output": [
                            {
                              "type": "message",
                              "role": "assistant",
                              "content": [
                                { "type": "output_text", "text": "El guardia asiente y te deja pasar." }
                              ]
                            }
                          ]
                        }
                        """, MediaType.APPLICATION_JSON));

        OpenAiMasterAdapter adapter = new OpenAiMasterAdapter(properties, builder, objectMapper, meterRegistry);

        String narration = adapter.narrate(new NarrationRequest("Plaza", "Hablo con el guardia", ResultGrade.EXITO, "[]"));

        assertEquals("El guardia asiente y te deja pasar.", narration);
        server.verify();
    }

    @Test
    void narrateThrowsWhenOpenAiIsUnavailable() {
        RestClient.Builder builder = RestClient.builder();
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        server.expect(requestTo("https://fake-openai.test/v1/responses"))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withServerError());

        OpenAiMasterAdapter adapter = new OpenAiMasterAdapter(properties, builder, objectMapper, meterRegistry);

        assertThrows(AiUnavailableException.class,
                () -> adapter.narrate(new NarrationRequest("Plaza", "Hablo con el guardia", ResultGrade.EXITO, "[]")));
    }

    @Test
    void interpretParsesStructuredJsonSchemaOutput() {
        UUID npcId = UUID.randomUUID();
        RestClient.Builder builder = RestClient.builder();
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        server.expect(requestTo("https://fake-openai.test/v1/responses"))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withSuccess("""
                        {
                          "output": [
                            {
                              "type": "message",
                              "role": "assistant",
                              "content": [
                                { "type": "output_text", "text": "{\\"actionType\\":\\"SOCIAL\\",\\"targetNpcId\\":\\"%s\\"}" }
                              ]
                            }
                          ]
                        }
                        """.formatted(npcId), MediaType.APPLICATION_JSON));

        OpenAiMasterAdapter adapter = new OpenAiMasterAdapter(properties, builder, objectMapper, meterRegistry);

        ActionIntent intent = adapter.interpret(new InterpretationRequest(
                "Plaza", "Hablo con el guardia", List.of(new VisibleNpc(npcId, "Guardia"))));

        assertEquals(ActionType.SOCIAL, intent.actionType());
        assertEquals(npcId, intent.targetNpcId());
        server.verify();
    }

    @Test
    void interpretDropsNonUuidTargetIdInsteadOfFailing() {
        RestClient.Builder builder = RestClient.builder();
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        server.expect(requestTo("https://fake-openai.test/v1/responses"))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withSuccess("""
                        {
                          "output": [
                            {
                              "type": "message",
                              "role": "assistant",
                              "content": [
                                { "type": "output_text", "text": "{\\"actionType\\":\\"EXPLORATION\\",\\"targetNpcId\\":\\"chair-999\\"}" }
                              ]
                            }
                          ]
                        }
                        """, MediaType.APPLICATION_JSON));

        OpenAiMasterAdapter adapter = new OpenAiMasterAdapter(properties, builder, objectMapper, meterRegistry);

        ActionIntent intent = adapter.interpret(new InterpretationRequest("Plaza", "Miro alrededor", List.of()));

        assertEquals(ActionType.EXPLORATION, intent.actionType());
        assertNull(intent.targetNpcId());
    }

    @Test
    void selectCandidateParsesStructuredJsonSchemaOutput() {
        UUID participantId = UUID.randomUUID();
        RestClient.Builder builder = RestClient.builder();
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        server.expect(requestTo("https://fake-openai.test/v1/responses"))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withSuccess("""
                        {
                          "output": [
                            {
                              "type": "message",
                              "role": "assistant",
                              "content": [
                                { "type": "output_text", "text": "{\\"candidateId\\":\\"%s\\"}" }
                              ]
                            }
                          ]
                        }
                        """.formatted(participantId), MediaType.APPLICATION_JSON));

        OpenAiMasterAdapter adapter = new OpenAiMasterAdapter(properties, builder, objectMapper, meterRegistry);

        String selected = adapter.selectCandidate(new CandidateSelectionRequest(
                "Ataco al cazador", List.of(new Candidate(participantId.toString(), "Cazador"))));

        assertEquals(participantId.toString(), selected);
        server.verify();
    }

    @Test
    void selectCandidateReturnsNullWhenModelDoesNotChooseOne() {
        RestClient.Builder builder = RestClient.builder();
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        server.expect(requestTo("https://fake-openai.test/v1/responses"))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withSuccess("""
                        {
                          "output": [
                            {
                              "type": "message",
                              "role": "assistant",
                              "content": [
                                { "type": "output_text", "text": "{\\"candidateId\\":null}" }
                              ]
                            }
                          ]
                        }
                        """, MediaType.APPLICATION_JSON));

        OpenAiMasterAdapter adapter = new OpenAiMasterAdapter(properties, builder, objectMapper, meterRegistry);

        String selected = adapter.selectCandidate(new CandidateSelectionRequest(
                "No sé qué hacer", List.of(new Candidate("1", "Cazador"))));

        assertNull(selected);
    }
}
