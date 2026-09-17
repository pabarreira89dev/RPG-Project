package pab.rpg.domain.rules;

// Allowed difficulty tiers per TDD MVP v0.2 section 8.3.
public enum Difficulty {
    TRIVIAL(8),
    EASY(11),
    MODERATE(14),
    HARD(17),
    VERY_HARD(20),
    EXTREME(23),
    LEGENDARY(26);

    private final int value;

    Difficulty(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}
