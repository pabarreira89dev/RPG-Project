package pab.rpg.domain.item;

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

// Session-scoped item instance: exactly one of ownerId (carried by the player character) or
// locationId (lying in the world) is set at any time, enforced by a DB check constraint.
@Entity
@Table(name = "item")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class Item {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private UUID sessionId;

    @Column(nullable = false)
    private UUID templateId;

    private UUID ownerId;

    private UUID locationId;

    @Column(nullable = false)
    private int quantity;

    private Integer durability;

    public void pickUp(UUID ownerId) {
        this.ownerId = ownerId;
        this.locationId = null;
    }

}
