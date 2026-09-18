package pab.rpg.api.dto.request;

// choiceKey is an optional override; if omitted, QuestController derives it from free text via
// QuestService.advanceQuestFromText.
public record AdvanceQuestRequest(String choiceKey, String text) {
}
