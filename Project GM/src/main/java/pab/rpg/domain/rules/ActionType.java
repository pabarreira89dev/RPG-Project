package pab.rpg.domain.rules;

// Minimal action categories from GDD section 5 (estados principales), each bound to the attribute and
// base difficulty it checks. Provisional until real text interpretation (OpenAI) selects these dynamically.
public enum ActionType {
    EXPLORATION(Attribute.PERCEPTION, Difficulty.EASY),
    SOCIAL(Attribute.PRESENCE, Difficulty.MODERATE),
    INVESTIGATION(Attribute.INTELLECT, Difficulty.MODERATE),
    PHYSICAL(Attribute.STRENGTH, Difficulty.HARD);

    private final Attribute attribute;
    private final Difficulty difficulty;

    ActionType(Attribute attribute, Difficulty difficulty) {
        this.attribute = attribute;
        this.difficulty = difficulty;
    }

    public Attribute getAttribute() {
        return attribute;
    }

    public Difficulty getDifficulty() {
        return difficulty;
    }
}
