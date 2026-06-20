package backend.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Entity
@Table(name = "army_list")
public class ArmyList {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "game_edition_id", nullable = false)
    private GameEdition gameEdition;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false, length = 80)
    private String faction;

    /**
     * For 10th edition this is the detachment/ruleset,
     * for example Gladius Task Force.
     *
     * For 9th edition this is the detachment type,
     * for example Battalion Detachment.
     */
    @Column(length = 80)
    private String armyRule;

    @Column(nullable = false)
    private Integer pointsLimit;

    @Column(nullable = false)
    private Integer totalPoints;

    @Column(length = 1000)
    private String description;

    @Builder.Default
    @OneToMany(mappedBy = "armyList", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ArmyListUnit> units = new ArrayList<>();

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public void replaceUnits(List<ArmyListUnit> newUnits) {
        units.clear();

        for (ArmyListUnit unit : newUnits) {
            unit.setArmyList(this);
            units.add(unit);
        }
    }
}