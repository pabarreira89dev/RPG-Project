package pab.rpg.domain.entity;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.AttributeOverrides;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
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

@Entity
@Table(name = "player_character")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class Character {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false)
    private int level;

    @Column(nullable = false)
    private int experience;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "strength", column = @Column(name = "strength", nullable = false)),
            @AttributeOverride(name = "agility", column = @Column(name = "agility", nullable = false)),
            @AttributeOverride(name = "intellect", column = @Column(name = "intellect", nullable = false)),
            @AttributeOverride(name = "willpower", column = @Column(name = "willpower", nullable = false)),
            @AttributeOverride(name = "perception", column = @Column(name = "perception", nullable = false)),
            @AttributeOverride(name = "presence", column = @Column(name = "presence", nullable = false))
    })
    private AttributeSet attributes;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "maximum", column = @Column(name = "health_maximum", nullable = false)),
            @AttributeOverride(name = "current", column = @Column(name = "health_current", nullable = false)),
            @AttributeOverride(name = "wounds", column = @Column(name = "health_wounds", nullable = false))
    })
    private HealthState health;

}
