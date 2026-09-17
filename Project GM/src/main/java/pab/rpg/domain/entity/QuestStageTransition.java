package pab.rpg.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

// A valid branch (edge) in a quest's state machine, selected by a player choiceKey.
@Entity
@Table(name = "quest_stage_transition")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class QuestStageTransition {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private UUID questId;

    @Column(nullable = false)
    private UUID fromStageId;

    @Column(nullable = false)
    private UUID toStageId;

    @Column(nullable = false, length = 60)
    private String choiceKey;

}
