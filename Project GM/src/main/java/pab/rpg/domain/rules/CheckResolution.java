package pab.rpg.domain.rules;

public record CheckResolution(
        long seed,
        int d20,
        int attributeModifier,
        int skillBonus,
        int circumstanceModifier,
        int total,
        Difficulty difficulty,
        int margin,
        ResultGrade grade
) {
}
