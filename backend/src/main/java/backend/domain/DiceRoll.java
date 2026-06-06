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
@Table(name = "dice_roll")
public class DiceRoll {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Roll history belongs to the logged-in user
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private DiceType diceType;

    @Column(nullable = false)
    private Integer diceCount;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "dice_roll_result", joinColumns = @JoinColumn(name = "dice_roll_id"))
    @OrderColumn(name = "result_index")
    @Column(name = "result_value", nullable = false)
    @Builder.Default
    private List<Integer> results = new ArrayList<>();

    @Column(length = 80)
    private String sourcePresetName;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    void onCreate() {
        createdAt = LocalDateTime.now();
    }
}