package pab.rpg.domain.rules;

// Result grade thresholds per TDD MVP v0.2 section 8.4.
public enum ResultGrade {
    GRAN_EXITO,
    EXITO,
    EXITO_CON_COSTE,
    FRACASO,
    FRACASO_GRAVE;

    public static ResultGrade fromMargin(int margin) {
        if (margin >= 6) {
            return GRAN_EXITO;
        }
        if (margin >= 0) {
            return EXITO;
        }
        if (margin >= -3) {
            return EXITO_CON_COSTE;
        }
        if (margin >= -7) {
            return FRACASO;
        }
        return FRACASO_GRAVE;
    }
}
