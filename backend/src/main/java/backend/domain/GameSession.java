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
@Table(name = "game_session")
public class GameSession {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, updatable = false)
    private LocalDateTime startedAt;

    @Column
    private LocalDateTime endedAt;

    @Column(length = 80)
    private String playerOneName;

    @Column(length = 80)
    private String playerTwoName;

    @Column(length = 80)
    private String playerOneFaction;

    @Column(length = 80)
    private String playerTwoFaction;

    @Column(length = 120)
    private String missionName;

    @Column(length = 120)
    private String deploymentMap;

    @Column(length = 1000)
    private String notes;

    @Builder.Default
    @Column(nullable = false)
    private Integer currentRound = 1;

    @Builder.Default
    @Column(nullable = false)
    private Integer playerOneScore = 0;

    @Builder.Default
    @Column(nullable = false)
    private Integer playerTwoScore = 0;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private GameSessionStatus status = GameSessionStatus.ACTIVE;

    @PrePersist
    void onCreate() {
        startedAt = LocalDateTime.now();
    }
}