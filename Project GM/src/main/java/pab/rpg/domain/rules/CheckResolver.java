package pab.rpg.domain.rules;

import org.springframework.stereotype.Component;

import java.security.SecureRandom;
import java.util.Random;

// Resolves d20 checks per TDD MVP v0.2 section 8: seed persisted for audit, deterministic roll from that seed.
@Component
public class CheckResolver {

    private final SecureRandom seedGenerator = new SecureRandom();

    public CheckResolution resolve(int attributeScore, int skillBonus, int circumstanceModifier, Difficulty difficulty) {
        long seed = seedGenerator.nextLong();
        int d20 = new Random(seed).nextInt(20) + 1;
        int attributeModifier = Math.floorDiv(attributeScore - 10, 2);
        int total = d20 + attributeModifier + skillBonus + circumstanceModifier;
        int margin = total - difficulty.getValue();
        ResultGrade grade = ResultGrade.fromMargin(margin);

        return new CheckResolution(seed, d20, attributeModifier, skillBonus, circumstanceModifier, total, difficulty, margin, grade);
    }
}
