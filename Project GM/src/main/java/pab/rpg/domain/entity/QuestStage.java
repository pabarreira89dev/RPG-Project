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

// A node in a quest's state machine (GDD sec. 23). Exactly one stage per quest is initial;
// terminal stages complete the quest when reached.
@Entity
@Table(name = "quest_stage")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class QuestStage {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private UUID questId;

    @Column(nullable = false, length = 60)
    private String code;

    @Column(nullable = false, length = 500)
    private String description;

    @Column(nullable = false)
    private boolean initial;

    @Column(nullable = false)
    private boolean terminal;

}
