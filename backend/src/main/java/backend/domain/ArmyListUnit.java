package backend.domain;

import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Entity
@Table(name = "army_list_unit")
public class ArmyListUnit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "army_list_id", nullable = false)
    private ArmyList armyList;

    @Column(nullable = false, length = 120)
    private String name;

    @Column(length = 80)
    private String unitType;

    @Column(nullable = false)
    private Integer points;

    @Column(nullable = false)
    private Integer quantity;

    @Column(length = 500)
    private String notes;
}