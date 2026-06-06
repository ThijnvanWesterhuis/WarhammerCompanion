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
        name = "dice_preset",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_dice_preset_user_name", columnNames = {"user_id", "name"})
        }
)
public class DicePreset {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Presets belong to the logged-in user
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, length = 80)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private DiceType diceType;

    @Column(nullable = false)
    private Integer diceCount;

    // Optional, useful later for Shooting/Fight/Charge phase filtering
    @Column(length = 50)
    private String phase;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    void onCreate() {
        createdAt = LocalDateTime.now();
    }
}