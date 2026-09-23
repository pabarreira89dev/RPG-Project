package pab.rpg.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pab.rpg.domain.character.Character;
import pab.rpg.domain.combat.Combat;
import pab.rpg.domain.combat.CombatParticipant;
import pab.rpg.domain.combat.CombatParticipantStatus;
import pab.rpg.domain.combat.CombatStatus;
import pab.rpg.domain.combat.CombatTeam;
import pab.rpg.domain.session.GameSession;
import pab.rpg.domain.world.Location;
import pab.rpg.domain.npc.Npc;
import pab.rpg.domain.npc.NpcStatus;
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
import pab.rpg.exception.CombatNotFoundException;
import pab.rpg.service.CombatService;
import pab.rpg.service.GameEventService;
import pab.rpg.service.MasterAdapter;
import pab.rpg.service.MasterAdapter.NarrationRequest;

import java.security.SecureRandom;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

// Combat rules per TDD MVP v0.2 section 8.5: d20 + Agility modifier initiative, 2 actions per turn,
// damage from the resolved check (no weapon system yet, so damage is a fixed table by result grade).
@Service
@RequiredArgsConstructor
@Transactional
public class CombatServiceImpl implements CombatService {

    private static final int ACTIONS_PER_TURN = 2;

    private final GameSessionRepository gameSessionRepository;
    private final CharacterRepository characterRepository;
    private final NpcRepository npcRepository;
    private final CombatRepository combatRepository;
    private final CombatParticipantRepository combatParticipantRepository;
    private final CheckResolver checkResolver;
    private final GameEventService gameEventService;
    private final LocationRepository locationRepository;
    private final MasterAdapter masterAdapter;
    private final SecureRandom initiativeRandom = new SecureRandom();

    @Override
    public CombatView startCombat(UUID sessionId, List<UUID> npcIds) {
        if (npcIds == null || npcIds.isEmpty()) {
            throw new IllegalArgumentException("npcIds must not be empty");
        }
        if (combatRepository.findBySessionIdAndStatus(sessionId, CombatStatus.ACTIVE).isPresent()) {
            throw new CombatNotAllowedException("Ya hay un combate activo en esta partida.");
        }

        GameSession session = gameSessionRepository.findById(sessionId)
                .orElseThrow(() -> new IllegalStateException("Session must already be validated by the caller"));

        List<Npc> opponents = npcIds.stream()
                .map(npcId -> requireOpponent(npcId, session.getCurrentLocationId()))
                .toList();

        Character character = session.getCharacter();
        List<Draft> drafts = new ArrayList<>();
        drafts.add(new Draft(CombatTeam.PLAYER, character.getId(), null, character.getName(),
                rollInitiative(character.getAttributes().getAgility()), character.getHealth().getMaximum()));
        for (Npc npc : opponents) {
            drafts.add(new Draft(CombatTeam.ENEMY, null, npc.getId(), npc.getName(),
                    rollInitiative(npc.getAttributes().getAgility()), npc.getHealthMaximum()));
        }
        drafts.sort(Comparator.comparingInt(Draft::initiative).reversed());

        Combat combat = combatRepository.save(new Combat(null, sessionId, CombatStatus.ACTIVE, 1, 0, Instant.now(), null));

        List<CombatParticipant> participants = new ArrayList<>();
        for (int turnOrder = 0; turnOrder < drafts.size(); turnOrder++) {
            Draft draft = drafts.get(turnOrder);
            participants.add(new CombatParticipant(
                    null, combat.getId(), draft.team(), draft.characterId(), draft.npcId(), draft.name(),
                    draft.initiative(), turnOrder, ACTIONS_PER_TURN, draft.healthMaximum(), draft.healthMaximum(),
                    CombatParticipantStatus.ACTIVE
            ));
        }
        participants = combatParticipantRepository.saveAll(participants);

        gameEventService.append(sessionId, "COMBAT_STARTED", session.getPlayerId(),
                Map.of("combatId", combat.getId(), "opponents", npcIds), session.getWorldTime());

        return toView(combat, participants, null);
    }

    @Override
    public CombatView performAttack(UUID sessionId, UUID combatId, UUID attackerParticipantId, UUID targetParticipantId) {
        GameSession session = gameSessionRepository.findById(sessionId)
                .orElseThrow(() -> new IllegalStateException("Session must already be validated by the caller"));

        Combat combat = combatRepository.findById(combatId)
                .filter(existing -> existing.getSessionId().equals(sessionId))
                .orElseThrow(() -> new CombatNotFoundException("El combate " + combatId + " no existe en esta partida."));

        if (combat.getStatus() != CombatStatus.ACTIVE) {
            throw new CombatNotAllowedException("El combate ya ha terminado.");
        }

        List<CombatParticipant> participants = combatParticipantRepository.findAllByCombatIdOrderByTurnOrderAsc(combatId);
        CombatParticipant attacker = findParticipant(participants, attackerParticipantId);
        CombatParticipant target = findParticipant(participants, targetParticipantId);

        if (attacker.getTurnOrder() != combat.getCurrentTurnOrder()) {
            throw new CombatNotAllowedException("No es el turno de este combatiente.");
        }
        if (attacker.getStatus() != CombatParticipantStatus.ACTIVE) {
            throw new CombatNotAllowedException("Este combatiente no puede actuar.");
        }
        if (attacker.getActionsRemaining() <= 0) {
            throw new CombatNotAllowedException("Este combatiente no tiene acciones disponibles.");
        }
        if (target.getTeam() == attacker.getTeam()) {
            throw new CombatNotAllowedException("No se puede atacar a un aliado.");
        }
        if (target.getStatus() != CombatParticipantStatus.ACTIVE) {
            throw new CombatNotAllowedException("El objetivo ya no puede ser atacado.");
        }

        CheckResolution resolution = checkResolver.resolve(attributeScore(attacker), 0, 0, Difficulty.MODERATE);
        int damage = damageFor(resolution.grade());

        target.applyDamage(damage);
        attacker.consumeAction();
        combatParticipantRepository.saveAll(List.of(attacker, target));

        gameEventService.append(sessionId, "COMBAT_ATTACK_RESOLVED", session.getPlayerId(), Map.of(
                "combatId", combatId,
                "attackerId", attacker.getId(),
                "targetId", target.getId(),
                "grade", resolution.grade().name(),
                "damage", damage,
                "targetHealthCurrent", target.getHealthCurrent(),
                "targetStatus", target.getStatus().name()
        ), session.getWorldTime());

        boolean enemyTeamDefeated = participants.stream()
                .filter(p -> p.getTeam() == CombatTeam.ENEMY)
                .allMatch(p -> p.getStatus() != CombatParticipantStatus.ACTIVE);
        boolean playerTeamDefeated = participants.stream()
                .filter(p -> p.getTeam() == CombatTeam.PLAYER)
                .allMatch(p -> p.getStatus() != CombatParticipantStatus.ACTIVE);

        if (enemyTeamDefeated || playerTeamDefeated) {
            combat.complete(Instant.now());
            gameEventService.append(sessionId, "COMBAT_ENDED", session.getPlayerId(), Map.of(
                    "combatId", combatId,
                    "outcome", enemyTeamDefeated ? "VICTORY" : "DEFEAT"
            ), session.getWorldTime());
        } else if (attacker.getActionsRemaining() == 0) {
            advanceTurn(combat, participants);
        }
        combatRepository.save(combat);

        String narration = masterAdapter.narrate(new NarrationRequest(
                sceneSummary(session.getCurrentLocationId()),
                attacker.getName() + " ataca a " + target.getName(),
                resolution.grade(),
                "daño=" + damage + ", estado objetivo=" + target.getStatus()
        ));

        return toView(combat, participants, narration);
    }

    @Override
    public CombatView performAttack(UUID sessionId, UUID combatId, String playerText) {
        Combat combat = combatRepository.findById(combatId)
                .filter(existing -> existing.getSessionId().equals(sessionId))
                .orElseThrow(() -> new CombatNotFoundException("El combate " + combatId + " no existe en esta partida."));

        List<CombatParticipant> participants = combatParticipantRepository.findAllByCombatIdOrderByTurnOrderAsc(combatId);

        CombatParticipant attacker = participants.stream()
                .filter(participant -> participant.getTurnOrder() == combat.getCurrentTurnOrder())
                .findFirst()
                .orElseThrow(() -> new CombatNotAllowedException("No hay un combatiente activo en este turno."));
        if (attacker.getTeam() != CombatTeam.PLAYER) {
            throw new CombatNotAllowedException("No es el turno del jugador.");
        }

        List<MasterAdapter.Candidate> candidates = participants.stream()
                .filter(participant -> participant.getTeam() == CombatTeam.ENEMY && participant.getStatus() == CombatParticipantStatus.ACTIVE)
                .map(participant -> new MasterAdapter.Candidate(participant.getId().toString(), participant.getName()))
                .toList();

        String selectedId = masterAdapter.selectCandidate(new MasterAdapter.CandidateSelectionRequest(playerText, candidates));
        UUID targetParticipantId = parseParticipantId(selectedId);
        if (targetParticipantId == null) {
            throw new CombatNotAllowedException("No se identifica un objetivo claro para atacar.");
        }

        return performAttack(sessionId, combatId, attacker.getId(), targetParticipantId);
    }

    // A hallucinated/non-UUID target is dropped here; a real-but-invalid participant id (wrong team,
    // already down) is still rejected by the existing checks in the 4-arg performAttack above.
    private UUID parseParticipantId(String rawId) {
        if (rawId == null || rawId.isBlank()) {
            return null;
        }
        try {
            return UUID.fromString(rawId);
        } catch (IllegalArgumentException exception) {
            return null;
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<CombatView> getActiveCombat(UUID sessionId) {
        return combatRepository.findBySessionIdAndStatus(sessionId, CombatStatus.ACTIVE)
                .map(combat -> toView(combat, combatParticipantRepository.findAllByCombatIdOrderByTurnOrderAsc(combat.getId()), null));
    }

    private String sceneSummary(UUID locationId) {
        Location location = locationRepository.findById(locationId)
                .orElseThrow(() -> new IllegalStateException("Location must already be validated by the caller"));
        return location.getName() + " — " + location.getDescription();
    }

    private Npc requireOpponent(UUID npcId, UUID locationId) {
        Npc npc = npcRepository.findById(npcId)
                .orElseThrow(() -> new CombatNotAllowedException("El NPC " + npcId + " no existe."));
        if (npc.getStatus() != NpcStatus.ALIVE) {
            throw new CombatNotAllowedException("El NPC " + npc.getCode() + " no puede combatir.");
        }
        if (!npc.getLocationId().equals(locationId)) {
            throw new CombatNotAllowedException("El NPC " + npc.getCode() + " no está en esta localización.");
        }
        return npc;
    }

    private int rollInitiative(int agilityScore) {
        int modifier = Math.floorDiv(agilityScore - 10, 2);
        int d20 = initiativeRandom.nextInt(20) + 1;
        return d20 + modifier;
    }

    private CombatParticipant findParticipant(List<CombatParticipant> participants, UUID participantId) {
        return participants.stream()
                .filter(participant -> participant.getId().equals(participantId))
                .findFirst()
                .orElseThrow(() -> new CombatNotAllowedException("El combatiente " + participantId + " no está en este combate."));
    }

    private int attributeScore(CombatParticipant participant) {
        if (participant.getCharacterId() != null) {
            return characterRepository.findById(participant.getCharacterId())
                    .orElseThrow(() -> new IllegalStateException("Character not found for combat participant"))
                    .getAttributes().getStrength();
        }
        return npcRepository.findById(participant.getNpcId())
                .orElseThrow(() -> new IllegalStateException("Npc not found for combat participant"))
                .getAttributes().getStrength();
    }

    private int damageFor(ResultGrade grade) {
        return switch (grade) {
            case GRAN_EXITO -> 8;
            case EXITO -> 5;
            case EXITO_CON_COSTE -> 3;
            case FRACASO, FRACASO_GRAVE -> 0;
        };
    }

    // Advances to the next ACTIVE participant in turn order; wrapping back to the start begins a new round.
    private void advanceTurn(Combat combat, List<CombatParticipant> participants) {
        int participantCount = participants.size();
        int current = combat.getCurrentTurnOrder();
        int next = current;
        boolean wrapped = false;

        for (int step = 1; step <= participantCount; step++) {
            int candidateOrder = (current + step) % participantCount;
            if (candidateOrder <= current) {
                wrapped = true;
            }
            if (participants.get(candidateOrder).getStatus() == CombatParticipantStatus.ACTIVE) {
                next = candidateOrder;
                break;
            }
        }

        if (wrapped) {
            participants.stream()
                    .filter(participant -> participant.getStatus() == CombatParticipantStatus.ACTIVE)
                    .forEach(CombatParticipant::resetActions);
            combatParticipantRepository.saveAll(participants);
            combat.startNewRound(next);
        } else {
            combat.advanceTurn(next);
        }
    }

    private CombatView toView(Combat combat, List<CombatParticipant> participants, String narration) {
        UUID currentParticipantId = participants.stream()
                .filter(participant -> participant.getTurnOrder() == combat.getCurrentTurnOrder())
                .map(CombatParticipant::getId)
                .findFirst()
                .orElse(null);

        List<ParticipantView> participantViews = participants.stream()
                .map(participant -> new ParticipantView(
                        participant.getId(), participant.getName(), participant.getTeam(), participant.getInitiative(),
                        participant.getTurnOrder(), participant.getActionsRemaining(), participant.getHealthCurrent(),
                        participant.getHealthMaximum(), participant.getStatus()
                ))
                .toList();

        return new CombatView(combat.getId(), combat.getStatus(), combat.getRoundNumber(), currentParticipantId, participantViews, narration);
    }

    private record Draft(CombatTeam team, UUID characterId, UUID npcId, String name, int initiative, int healthMaximum) {
    }
}
