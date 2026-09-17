package pab.rpg.service.impl;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
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
import pab.rpg.service.QuestService.QuestStateView;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class QuestServiceImplTest {

    @Mock
    private QuestRepository questRepository;
    @Mock
    private QuestStageRepository questStageRepository;
    @Mock
    private QuestStageTransitionRepository questStageTransitionRepository;
    @Mock
    private QuestStateRepository questStateRepository;

    private QuestServiceImpl service() {
        return new QuestServiceImpl(questRepository, questStageRepository, questStageTransitionRepository, questStateRepository);
    }

    private Quest quest(UUID id) {
        return new Quest(id, "aron_debt", "La deuda de Aron", "Aron debe dinero.");
    }

    private QuestStage stage(UUID id, UUID questId, String code, boolean initial, boolean terminal) {
        return new QuestStage(id, questId, code, "Descripción de " + code, initial, terminal);
    }

    @Test
    void startQuestThrowsWhenQuestCodeIsUnknown() {
        when(questRepository.findByCode("unknown")).thenReturn(Optional.empty());

        assertThrows(QuestNotFoundException.class, () -> service().startQuest(UUID.randomUUID(), "unknown"));
    }

    @Test
    void startQuestCreatesStateAtInitialStageWhenNotStarted() {
        UUID sessionId = UUID.randomUUID();
        UUID questId = UUID.randomUUID();
        UUID initialStageId = UUID.randomUUID();
        Quest quest = quest(questId);
        QuestStage initialStage = stage(initialStageId, questId, "quest_started", true, false);

        when(questRepository.findByCode("aron_debt")).thenReturn(Optional.of(quest));
        when(questStateRepository.findBySessionIdAndQuestId(sessionId, questId)).thenReturn(Optional.empty());
        when(questStageRepository.findByQuestIdAndInitialTrue(questId)).thenReturn(Optional.of(initialStage));
        when(questStateRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(questStageRepository.findById(initialStageId)).thenReturn(Optional.of(initialStage));

        QuestStateView view = service().startQuest(sessionId, "aron_debt");

        assertEquals("aron_debt", view.questCode());
        assertEquals("quest_started", view.stageCode());
        assertEquals(QuestStatus.ACTIVE, view.status());
        verify(questStateRepository, times(1)).save(any());
    }

    @Test
    void startQuestIsIdempotentWhenAlreadyStarted() {
        UUID sessionId = UUID.randomUUID();
        UUID questId = UUID.randomUUID();
        UUID stageId = UUID.randomUUID();
        Quest quest = quest(questId);
        QuestState existing = new QuestState(UUID.randomUUID(), sessionId, questId, stageId, QuestStatus.ACTIVE, Instant.now(), Instant.now());
        QuestStage stage = stage(stageId, questId, "quest_started", true, false);

        when(questRepository.findByCode("aron_debt")).thenReturn(Optional.of(quest));
        when(questStateRepository.findBySessionIdAndQuestId(sessionId, questId)).thenReturn(Optional.of(existing));
        when(questStageRepository.findById(stageId)).thenReturn(Optional.of(stage));

        service().startQuest(sessionId, "aron_debt");

        verify(questStateRepository, never()).save(any());
        verify(questStageRepository, never()).findByQuestIdAndInitialTrue(any());
    }

    @Test
    void advanceQuestThrowsWhenQuestHasNotStarted() {
        UUID sessionId = UUID.randomUUID();
        UUID questId = UUID.randomUUID();
        when(questRepository.findByCode("aron_debt")).thenReturn(Optional.of(quest(questId)));
        when(questStateRepository.findBySessionIdAndQuestId(sessionId, questId)).thenReturn(Optional.empty());

        assertThrows(QuestTransitionNotAllowedException.class, () -> service().advanceQuest(sessionId, "aron_debt", "pay"));
    }

    @Test
    void advanceQuestThrowsWhenChoiceKeyIsNotAValidTransition() {
        UUID sessionId = UUID.randomUUID();
        UUID questId = UUID.randomUUID();
        UUID stageId = UUID.randomUUID();
        QuestState state = new QuestState(UUID.randomUUID(), sessionId, questId, stageId, QuestStatus.ACTIVE, Instant.now(), Instant.now());

        when(questRepository.findByCode("aron_debt")).thenReturn(Optional.of(quest(questId)));
        when(questStateRepository.findBySessionIdAndQuestId(sessionId, questId)).thenReturn(Optional.of(state));
        when(questStageTransitionRepository.findByQuestIdAndFromStageIdAndChoiceKey(questId, stageId, "unknown"))
                .thenReturn(Optional.empty());

        assertThrows(QuestTransitionNotAllowedException.class, () -> service().advanceQuest(sessionId, "aron_debt", "unknown"));
    }

    @Test
    void advanceQuestThrowsWhenQuestIsAlreadyCompleted() {
        UUID sessionId = UUID.randomUUID();
        UUID questId = UUID.randomUUID();
        UUID stageId = UUID.randomUUID();
        QuestState state = new QuestState(UUID.randomUUID(), sessionId, questId, stageId, QuestStatus.COMPLETED, Instant.now(), Instant.now());

        when(questRepository.findByCode("aron_debt")).thenReturn(Optional.of(quest(questId)));
        when(questStateRepository.findBySessionIdAndQuestId(sessionId, questId)).thenReturn(Optional.of(state));

        assertThrows(QuestTransitionNotAllowedException.class, () -> service().advanceQuest(sessionId, "aron_debt", "pay"));
    }

    @Test
    void advanceQuestMovesToNextStageAndCompletesOnTerminalStage() {
        UUID sessionId = UUID.randomUUID();
        UUID questId = UUID.randomUUID();
        UUID fromStageId = UUID.randomUUID();
        UUID toStageId = UUID.randomUUID();
        QuestState state = new QuestState(UUID.randomUUID(), sessionId, questId, fromStageId, QuestStatus.ACTIVE, Instant.now(), Instant.now());
        QuestStageTransition transition = new QuestStageTransition(UUID.randomUUID(), questId, fromStageId, toStageId, "pay");
        QuestStage nextStage = stage(toStageId, questId, "debt_paid", false, true);

        when(questRepository.findByCode("aron_debt")).thenReturn(Optional.of(quest(questId)));
        when(questStateRepository.findBySessionIdAndQuestId(sessionId, questId)).thenReturn(Optional.of(state));
        when(questStageTransitionRepository.findByQuestIdAndFromStageIdAndChoiceKey(questId, fromStageId, "pay"))
                .thenReturn(Optional.of(transition));
        when(questStageRepository.findById(toStageId)).thenReturn(Optional.of(nextStage));

        QuestStateView view = service().advanceQuest(sessionId, "aron_debt", "pay");

        assertEquals("debt_paid", view.stageCode());
        assertEquals(QuestStatus.COMPLETED, view.status());
        verify(questStateRepository, times(1)).save(state);
    }

    @Test
    void getVisibleQuestsMapsEachSessionState() {
        UUID sessionId = UUID.randomUUID();
        UUID questId = UUID.randomUUID();
        UUID stageId = UUID.randomUUID();
        QuestState state = new QuestState(UUID.randomUUID(), sessionId, questId, stageId, QuestStatus.ACTIVE, Instant.now(), Instant.now());

        when(questStateRepository.findAllBySessionId(sessionId)).thenReturn(List.of(state));
        when(questRepository.findById(questId)).thenReturn(Optional.of(quest(questId)));
        when(questStageRepository.findById(stageId)).thenReturn(Optional.of(stage(stageId, questId, "quest_started", true, false)));

        List<QuestStateView> views = service().getVisibleQuests(sessionId);

        assertEquals(1, views.size());
        assertEquals("aron_debt", views.get(0).questCode());
    }
}
