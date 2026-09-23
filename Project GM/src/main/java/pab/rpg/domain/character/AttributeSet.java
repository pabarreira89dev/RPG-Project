package pab.rpg.domain.character;

import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Embeddable
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class AttributeSet {

    private int strength;
    private int agility;
    private int intellect;
    private int willpower;
    private int perception;
    private int presence;

}
