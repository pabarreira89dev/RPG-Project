package pab.rpg.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

// NPCs are a shared catalog (like Location), not duplicated per session; per-session state
// (relationship, known facts) lives in Relationship/NpcKnowledgeFact instead.
@Entity
@Table(name = "npc")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class Npc {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true, length = 60)
    private String code;

    @Column(nullable = false, length = 120)
    private String name;

    @Column(nullable = false)
    private UUID locationId;

    @Column(length = 60)
    private String faction;

    @Column(nullable = false, length = 500)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private NpcStatus status;

    // Needed for combat (initiative, attack rolls); defaults were backfilled for the pre-existing seed NPCs.
    @Embedded
    private AttributeSet attributes;

    @Column(nullable = false)
    private int healthMaximum;

}
