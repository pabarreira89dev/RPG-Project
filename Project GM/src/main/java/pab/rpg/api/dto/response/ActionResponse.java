package pab.rpg.api.dto.response;

import java.util.List;
import java.util.UUID;

public record ActionResponse(
        UUID actionId,
        String status,
        String narration,
        ResultDetails result,
        List<String> events,
        long stateVersion
) {

    public record ResultDetails(String type, RollDetails roll) {
    }

    public record RollDetails(int d20, int modifier, int total, int difficulty) {
    }
}
