package backend.domain;

import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Entity
@Table(
        name = "unit_catalog",
        indexes = {
                @Index(name = "idx_unit_catalog_edition_faction", columnList = "game_edition_id, faction")
        }
)
public class UnitCatalog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Example:
     * 10TH Intercessor Squad = 80 points
     * 9TH Intercessor Squad = 90 points
     *
     * Same unit name can exist multiple times because points/rules differ per edition.
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "game_edition_id", nullable = false)
    private GameEdition gameEdition;

    @Column(nullable = false, length = 80)
    private String faction;

    @Column(nullable = false, length = 120)
    private String name;

    @Column(nullable = false, length = 80)
    private String unitType;

    @Column(nullable = false)
    private Integer points;

    @Column(length = 80)
    private String models;

    @Column(length = 500)
    private String keywords;
}