package pab.rpg.domain.combat;

// DEAD is reserved for future death/stabilization rules (GDD section 12); combat only produces DOWNED for now.
public enum CombatParticipantStatus {
    ACTIVE,
    DOWNED,
    DEAD
}
