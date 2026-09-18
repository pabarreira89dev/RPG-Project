package pab.rpg.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pab.rpg.domain.entity.Quest;
import pab.rpg.domain.entity.QuestStage;
import pab.rpg.domain.entity.QuestStageTransition;
import pab.rpg.domain.entity.QuestState;
import pab.rpg.domain.entity.QuestStatus;
import pab.rpg.domain.repository.QuestRepository;
import pab.rpg.domain.repository.QuestStageRepository;
import pab.rpg.domain.repository.QuestStageTransitionRepository;
import pab.rpg.domain.repository.QuestStateRepository;
import pab.rpg.exception.QuestNotFoundException;
import pab.rpg.exception.QuestTransitionNotAllowedException;
import pab.rpg.service.MasterAdapter;
import pab.rpg.service.QuestService;

import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class QuestServiceImpl implements QuestService {

    private final QuestRepository questRepository;
    private final QuestStageRepository questStageRepository;
    private final QuestStageTransitionRepository questStageTransitionRepository;
    private final QuestStateRepository questStateRepository;
    private final MasterAdapter masterAdapter;

    @Override
    public QuestStateView startQuest(UUID sessionId, String questCode) {
        Quest quest = requireQuest(questCode);

        QuestState state = questStateRepository.findBySessionIdAndQuestId(sessionId, quest.getId())
                .orElseGet(() -> {
                    QuestStage initialStage = questStageRepository.findByQuestIdAndInitialTrue(quest.getId())
                            .orElseThrow(() -> new IllegalStateException("Quest " + questCode + " has no initial stage"));
                    Instant now = Instant.now();
                    return questStateRepository.save(new QuestState(
                            null, sessionId, quest.getId(), initialStage.getId(), QuestStatus.ACTIVE, now, now
                    ));
                });

        return toView(quest, state);
    }

    @Override
    public QuestStateView advanceQuest(UUID sessionId, String questCode, String choiceKey) {
        Objects.requireNonNull(choiceKey, "choiceKey must not be null");

        Quest quest = requireQuest(questCode);
        QuestState state = questStateRepository.findBySessionIdAndQuestId(sessionId, quest.getId())
                .orElseThrow(() -> new QuestTransitionNotAllowedException("La misión no ha empezado todavía."));

        if (state.getStatus() != QuestStatus.ACTIVE) {
            throw new QuestTransitionNotAllowedException("La misión ya ha finalizado.");
        }

        QuestStageTransition transition = questStageTransitionRepository
                .findByQuestIdAndFromStageIdAndChoiceKey(quest.getId(), state.getCurrentStageId(), choiceKey)
                .orElseThrow(() -> new QuestTransitionNotAllowedException("Esa decisión no está disponible en esta etapa."));

        QuestStage nextStage = questStageRepository.findById(transition.getToStageId())
                .orElseThrow(() -> new IllegalStateException("Transition points to a missing stage"));

        state.advanceTo(nextStage.getId(), nextStage.isTerminal(), Instant.now());
        questStateRepository.save(state);

        return toView(quest, state);
    }

    @Override
    public QuestStateView advanceQuestFromText(UUID sessionId, String questCode, String playerText) {
        Quest quest = requireQuest(questCode);
        QuestState state = questStateRepository.findBySessionIdAndQuestId(sessionId, quest.getId())
                .orElseThrow(() -> new QuestTransitionNotAllowedException("La misión no ha empezado todavía."));

        if (state.getStatus() != QuestStatus.ACTIVE) {
            throw new QuestTransitionNotAllowedException("La misión ya ha finalizado.");
        }

        List<QuestStageTransition> transitions =
                questStageTransitionRepository.findAllByQuestIdAndFromStageId(quest.getId(), state.getCurrentStageId());

        List<MasterAdapter.Candidate> candidates = transitions.stream()
                .map(transition -> new MasterAdapter.Candidate(transition.getChoiceKey(), transition.getChoiceKey()))
                .toList();

        String choiceKey = masterAdapter.selectCandidate(new MasterAdapter.CandidateSelectionRequest(playerText, candidates));
        if (choiceKey == null) {
            throw new QuestTransitionNotAllowedException("No se identifica una decisión clara para esta etapa.");
        }

        return advanceQuest(sessionId, questCode, choiceKey);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<QuestStateView> getQuestState(UUID sessionId, String questCode) {
        Quest quest = requireQuest(questCode);

        return questStateRepository.findBySessionIdAndQuestId(sessionId, quest.getId())
                .map(state -> toView(quest, state));
    }

    @Override
    @Transactional(readOnly = true)
    public List<QuestStateView> getVisibleQuests(UUID sessionId) {
        return questStateRepository.findAllBySessionId(sessionId).stream()
                .map(state -> toView(requireQuestById(state.getQuestId()), state))
                .toList();
    }

    private Quest requireQuest(String questCode) {
        return questRepository.findByCode(questCode)
                .orElseThrow(() -> new QuestNotFoundException(questCode));
    }

    private Quest requireQuestById(UUID questId) {
        return questRepository.findById(questId)
                .orElseThrow(() -> new IllegalStateException("Quest state points to a missing quest"));
    }

    private QuestStateView toView(Quest quest, QuestState state) {
        QuestStage stage = questStageRepository.findById(state.getCurrentStageId())
                .orElseThrow(() -> new IllegalStateException("Quest state points to a missing stage"));

        return new QuestStateView(quest.getCode(), quest.getTitle(), stage.getCode(), stage.getDescription(), state.getStatus());
    }
}
