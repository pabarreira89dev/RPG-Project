package pab.rpg.domain.rules;

import org.junit.jupiter.api.RepeatedTest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CheckResolverTest {

    private final CheckResolver checkResolver = new CheckResolver();

    @RepeatedTest(20)
    void resolveProducesConsistentFormula() {
        CheckResolution resolution = checkResolver.resolve(14, 1, 1, Difficulty.MODERATE);

        assertTrue(resolution.d20() >= 1 && resolution.d20() <= 20);
        assertEquals(2, resolution.attributeModifier());
        assertEquals(resolution.d20() + 2 + 1 + 1, resolution.total());
        assertEquals(resolution.total() - Difficulty.MODERATE.getValue(), resolution.margin());
        assertEquals(ResultGrade.fromMargin(resolution.margin()), resolution.grade());
    }
}
