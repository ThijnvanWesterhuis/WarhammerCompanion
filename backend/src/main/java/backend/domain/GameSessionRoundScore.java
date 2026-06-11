package backend.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Entity
@Table(
        name = "game_session_round_score",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_game_session_round_score_session_round",
                        columnNames = {"game_session_id", "round_number"}
                )
        }
)
public class GameSessionRoundScore {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "game_session_id", nullable = false)
    private GameSession gameSession;

    @Column(nullable = false)
    private Integer roundNumber;

    @Column(nullable = false)
    private Integer playerOneScore;

    @Column(nullable = false)
    private Integer playerTwoScore;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    @PreUpdate
    void updateTimestamp() {
        updatedAt = LocalDateTime.now();
    }
}