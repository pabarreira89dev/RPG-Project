package pab.rpg.domain.rules;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ResultGradeTest {

    @ParameterizedTest
    @CsvSource({
            "6, GRAN_EXITO",
            "10, GRAN_EXITO",
            "0, EXITO",
            "5, EXITO",
            "-1, EXITO_CON_COSTE",
            "-3, EXITO_CON_COSTE",
            "-4, FRACASO",
            "-7, FRACASO",
            "-8, FRACASO_GRAVE",
            "-20, FRACASO_GRAVE"
    })
    void fromMarginMapsToExpectedGrade(int margin, ResultGrade expected) {
        assertEquals(expected, ResultGrade.fromMargin(margin));
    }
}
