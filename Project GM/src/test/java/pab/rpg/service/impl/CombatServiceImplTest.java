package pab.rpg.service.impl;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pab.rpg.domain.character.AttributeSet;
import pab.rpg.domain.character.Character;
import pab.rpg.domain.combat.Combat;
import pab.rpg.domain.combat.CombatParticipant;
import pab.rpg.domain.combat.CombatParticipantStatus;
import pab.rpg.domain.combat.CombatStatus;
import pab.rpg.domain.combat.CombatTeam;
import pab.rpg.domain.session.GameSession;
import pab.rpg.domain.character.HealthState;
import pab.rpg.domain.world.Location;
import pab.rpg.domain.npc.Npc;
import pab.rpg.domain.npc.NpcStatus;
import pab.rpg.domain.session.SessionStatus;
import pab.rpg.domain.repository.CharacterRepository;
import pab.rpg.domain.repository.CombatParticipantRepository;
import pab.rpg.domain.repository.CombatRepository;
import pab.rpg.domain.repository.GameSessionRepository;
import pab.rpg.domain.repository.LocationRepository;
import pab.rpg.domain.repository.NpcRepository;
import pab.rpg.domain.rules.CheckResolution;
import pab.rpg.domain.rules.CheckResolver;
import pab.rpg.domain.rules.Difficulty;
import pab.rpg.domain.rules.ResultGrade;
import pab.rpg.exception.CombatNotAllowedException;
import pab.rpg.service.CombatService.CombatView;
import pab.rpg.service.GameEventService;
import pab.rpg.service.MasterAdapter;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CombatServiceImplTest {

    @Mock
    private GameSessionRepository gameSessionRepository;
    @Mock
    private CharacterRepository characterRepository;
    @Mock
    private NpcRepository npcRepository;
    @Mock
    private CombatRepository combatRepository;
    @Mock
    private CombatParticipantRepository combatParticipantRepository;
    @Mock
    private CheckResolver checkResolver;
    @Mock
    private GameEventService gameEventService;
    @Mock
    private LocationRepository locationRepository;
    @Mock
    private MasterAdapter masterAdapter;

    private CombatServiceImpl service() {
        return new CombatServiceImpl(
                gameSessionRepository, characterRepository, npcRepository, combatRepository,
                combatParticipantRepository, checkResolver, gameEventService, locationRepository, masterAdapter
        );
    }

    private GameSession session(UUID id, UUID locationId) {
        Character character = new Character(
                UUID.randomUUID(), "Aren", 1, 0,
                new AttributeSet(14, 10, 10, 10, 10, 10),
                new HealthState(20, 20, 0)
        );
        return new GameSession(
                id, UUID.randomUUID(), UUID.randomUUID(), locationId,
                SessionStatus.ACTIVE, Instant.parse("2026-01-01T08:00:00Z"), 0L, character
        );
    }

    private Npc npc(UUID id, UUID locationId, NpcStatus status) {
        return new Npc(id, "forest_hunter", "Cazador", locationId, null, "desc", status,
                new AttributeSet(12, 10, 10, 10, 10, 10), 24);
    }

    @Test
    void startCombatThrowsWhenAnotherCombatIsAlreadyActive() {
        UUID sessionId = UUID.randomUUID();
        when(combatRepository.findBySessionIdAndStatus(sessionId, CombatStatus.ACTIVE))
                .thenReturn(Optional.of(new Combat(UUID.randomUUID(), sessionId, CombatStatus.ACTIVE, 1, 0, Instant.now(), null)));

        assertThrows(CombatNotAllowedException.class, () -> service().startCombat(sessionId, List.of(UUID.randomUUID())));
    }

    @Test
    void startCombatThrowsWhenNpcIsNotAtCurrentLocation() {
        UUID sessionId = UUID.randomUUID();
        UUID locationId = UUID.randomUUID();
        UUID npcId = UUID.randomUUID();
        when(combatRepository.findBySessionIdAndStatus(sessionId, CombatStatus.ACTIVE)).thenReturn(Optional.empty());
        when(gameSessionRepository.findById(sessionId)).thenReturn(Optional.of(session(sessionId, locationId)));
        when(npcRepository.findById(npcId)).thenReturn(Optional.of(npc(npcId, UUID.randomUUID(), NpcStatus.ALIVE)));

        assertThrows(CombatNotAllowedException.class, () -> service().startCombat(sessionId, List.of(npcId)));
    }

    @Test
    void startCombatCreatesOrderedParticipantsAndAppendsEvent() {
        UUID sessionId = UUID.randomUUID();
        UUID locationId = UUID.randomUUID();
        UUID npcId = UUID.randomUUID();
        GameSession session = session(sessionId, locationId);
 
        when(combatRepository.findBySessionIdAndStatus(sessionId, CombatStatus.ACTIVE)).thenReturn(Optional.empty());
        when(gameSessionRepository.findById(sessionId)).thenReturn(Optional.of(session));
        when(npcRepository.findById(npcId)).thenReturn(Optional.of(npc(npcId, locationId, NpcStatus.ALIVE)));
        when(combatRepository.save(any())).thenAnswer(invocation -> {
            Combat combat = invocation.getArgument(0);
            return new Combat(UUID.randomUUID(), combat.getSessionId(), combat.getStatus(), combat.getRoundNumber(),
                    combat.getCurrentTurnOrder(), combat.getCreatedAt(), combat.getEndedAt());
        });
        when(combatParticipantRepository.saveAll(any())).thenAnswer(invocation -> {
            List<CombatParticipant> participants = invocation.getArgument(0);
            return participants.stream()
                    .map(p -> new CombatParticipant(UUID.randomUUID(), p.getCombatId(), p.getTeam(), p.getCharacterId(),
                            p.getNpcId(), p.getName(), p.getInitiative(), p.getTurnOrder(), p.getActionsRemaining(),
                            p.getHealthCurrent(), p.getHealthMaximum(), p.getStatus()))
                    .toList();
        });
 
        CombatView view = service().startCombat(sessionId, List.of(npcId));
 
        assertEquals(2, view.participants().size());
        assertEquals(CombatStatus.ACTIVE, view.status());
        assertEquals(1, view.roundNumber());
    }

    @Test
    void performAttackAppliesDamageAndEndsCombatWhenEnemyTeamIsDefeated() {
        UUID sessionId = UUID.randomUUID();
        UUID combatId = UUID.randomUUID();
        UUID characterId = UUID.randomUUID();
        UUID attackerId = UUID.randomUUID();
        UUID targetId = UUID.randomUUID();

        GameSession session = session(sessionId, UUID.randomUUID());
        Combat combat = new Combat(combatId, sessionId, CombatStatus.ACTIVE, 1, 0, Instant.now(), null);
        CombatParticipant attacker = new CombatParticipant(attackerId, combatId, CombatTeam.PLAYER, characterId, null,
                "Aren", 15, 0, 2, 20, 20, CombatParticipantStatus.ACTIVE);
        CombatParticipant target = new CombatParticipant(targetId, combatId, CombatTeam.ENEMY, null, UUID.randomUUID(),
                "Cazador", 10, 1, 2, 5, 24, CombatParticipantStatus.ACTIVE);

        when(gameSessionRepository.findById(sessionId)).thenReturn(Optional.of(session));
        when(combatRepository.findById(combatId)).thenReturn(Optional.of(combat));
        when(combatParticipantRepository.findAllByCombatIdOrderByTurnOrderAsc(combatId)).thenReturn(List.of(attacker, target));
        when(characterRepository.findById(characterId)).thenReturn(Optional.of(session.getCharacter()));
        when(checkResolver.resolve(anyInt(), anyInt(), anyInt(), any()))
                .thenReturn(new CheckResolution(1L, 18, 2, 0, 0, 20, Difficulty.MODERATE, 6, ResultGrade.GRAN_EXITO));
        when(combatParticipantRepository.saveAll(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(locationRepository.findById(session.getCurrentLocationId()))
                .thenReturn(Optional.of(new Location(session.getCurrentLocationId(), "forest_edge", "Linde del bosque", "Un claro entre árboles.")));
        when(masterAdapter.narrate(any())).thenReturn("El ataque acierta de lleno.");

        CombatView view = service().performAttack(sessionId, combatId, attackerId, targetId);

        assertEquals(CombatStatus.COMPLETED, view.status());
        assertEquals(0, view.participants().stream()
                .filter(p -> p.id().equals(targetId)).findFirst().orElseThrow().healthCurrent());
    }

    @Test
    void performAttackFromTextResolvesTargetViaMasterAdapter() {
        UUID sessionId = UUID.randomUUID();
        UUID combatId = UUID.randomUUID();
        UUID characterId = UUID.randomUUID();
        UUID attackerId = UUID.randomUUID();
        UUID targetId = UUID.randomUUID();

        GameSession session = session(sessionId, UUID.randomUUID());
        Combat combat = new Combat(combatId, sessionId, CombatStatus.ACTIVE, 1, 0, Instant.now(), null);
        CombatParticipant attacker = new CombatParticipant(attackerId, combatId, CombatTeam.PLAYER, characterId, null,
                "Aren", 15, 0, 2, 20, 20, CombatParticipantStatus.ACTIVE);
        CombatParticipant target = new CombatParticipant(targetId, combatId, CombatTeam.ENEMY, null, UUID.randomUUID(),
                "Cazador", 10, 1, 2, 5, 24, CombatParticipantStatus.ACTIVE);

        when(gameSessionRepository.findById(sessionId)).thenReturn(Optional.of(session));
        when(combatRepository.findById(combatId)).thenReturn(Optional.of(combat));
        when(combatParticipantRepository.findAllByCombatIdOrderByTurnOrderAsc(combatId)).thenReturn(List.of(attacker, target));
        when(characterRepository.findById(characterId)).thenReturn(Optional.of(session.getCharacter()));
        when(checkResolver.resolve(anyInt(), anyInt(), anyInt(), any()))
                .thenReturn(new CheckResolution(1L, 12, 2, 0, 0, 14, Difficulty.MODERATE, 0, ResultGrade.EXITO));
        when(combatParticipantRepository.saveAll(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(locationRepository.findById(session.getCurrentLocationId()))
                .thenReturn(Optional.of(new Location(session.getCurrentLocationId(), "forest_edge", "Linde del bosque", "Un claro entre árboles.")));
        when(masterAdapter.narrate(any())).thenReturn("El ataque acierta.");
        when(masterAdapter.selectCandidate(any())).thenReturn(targetId.toString());

        CombatView view = service().performAttack(sessionId, combatId, "Ataco al cazador");

        assertEquals(targetId, view.participants().stream()
                .filter(p -> p.healthCurrent() < p.healthMaximum()).findFirst().orElseThrow().id());
    }

    @Test
    void performAttackFromTextThrowsWhenNoTargetMatches() {
        UUID sessionId = UUID.randomUUID();
        UUID combatId = UUID.randomUUID();
        UUID attackerId = UUID.randomUUID();
        UUID targetId = UUID.randomUUID();

        Combat combat = new Combat(combatId, sessionId, CombatStatus.ACTIVE, 1, 0, Instant.now(), null);
        CombatParticipant attacker = new CombatParticipant(attackerId, combatId, CombatTeam.PLAYER, UUID.randomUUID(), null,
                "Aren", 15, 0, 2, 20, 20, CombatParticipantStatus.ACTIVE);
        CombatParticipant target = new CombatParticipant(targetId, combatId, CombatTeam.ENEMY, null, UUID.randomUUID(),
                "Cazador", 10, 1, 2, 24, 24, CombatParticipantStatus.ACTIVE);

        when(combatRepository.findById(combatId)).thenReturn(Optional.of(combat));
        when(combatParticipantRepository.findAllByCombatIdOrderByTurnOrderAsc(combatId)).thenReturn(List.of(attacker, target));
        when(masterAdapter.selectCandidate(any())).thenReturn(null);

        assertThrows(CombatNotAllowedException.class,
                () -> service().performAttack(sessionId, combatId, "No sé a quién atacar"));
    }

    @Test
    void performAttackThrowsWhenItIsNotAttackersTurn() {
        UUID sessionId = UUID.randomUUID();
        UUID combatId = UUID.randomUUID();
        UUID attackerId = UUID.randomUUID();
        UUID targetId = UUID.randomUUID();

        GameSession session = session(sessionId, UUID.randomUUID());
        Combat combat = new Combat(combatId, sessionId, CombatStatus.ACTIVE, 1, 1, Instant.now(), null);
        CombatParticipant attacker = new CombatParticipant(attackerId, combatId, CombatTeam.PLAYER, UUID.randomUUID(), null,
                "Aren", 15, 0, 2, 20, 20, CombatParticipantStatus.ACTIVE);
        CombatParticipant target = new CombatParticipant(targetId, combatId, CombatTeam.ENEMY, null, UUID.randomUUID(),
                "Cazador", 10, 1, 2, 24, 24, CombatParticipantStatus.ACTIVE);

        when(gameSessionRepository.findById(sessionId)).thenReturn(Optional.of(session));
        when(combatRepository.findById(combatId)).thenReturn(Optional.of(combat));
        when(combatParticipantRepository.findAllByCombatIdOrderByTurnOrderAsc(combatId)).thenReturn(List.of(attacker, target));

        assertThrows(CombatNotAllowedException.class, () -> service().performAttack(sessionId, combatId, attackerId, targetId));
    }
}
