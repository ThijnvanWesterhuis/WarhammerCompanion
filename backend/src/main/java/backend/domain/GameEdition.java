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
        name = "game_edition",
        uniqueConstraints = @UniqueConstraint(name = "uk_game_edition_code", columnNames = "code")
)
public class GameEdition {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 20)
    private String code;

    @Column(nullable = false, length = 80)
    private String displayName;

    @Column(length = 500)
    private String description;

    @Column(nullable = false)
    private Integer releaseOrder;
}