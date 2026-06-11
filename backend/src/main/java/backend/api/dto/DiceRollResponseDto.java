package backend.api.dto;

import backend.domain.DiceRoll;
import backend.domain.DiceType;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@AllArgsConstructor
public class DiceRollResponseDto {
    private Long id;
    private Long gameSessionId;
    private DiceType diceType;
    private Integer diceCount;
    private List<Integer> results;
    private Integer total;
    private Integer successThreshold;
    private Integer successCount;
    private Integer failCount;
    private String sourcePresetName;
    private Long rerollSourceRollId;
    private String rerollType;
    private LocalDateTime createdAt;

    public static DiceRollResponseDto fromRoll(DiceRoll roll) {
        int total = roll.getResults().stream()
                .mapToInt(Integer::intValue)
                .sum();

        Integer successCount = null;
        Integer failCount = null;

        if (roll.getSuccessThreshold() != null) {
            successCount = (int) roll.getResults().stream()
                    .filter(result -> result >= roll.getSuccessThreshold())
                    .count();

            failCount = roll.getResults().size() - successCount;
        }

        return new DiceRollResponseDto(
                roll.getId(),
                roll.getGameSession() == null ? null : roll.getGameSession().getId(),
                roll.getDiceType(),
                roll.getDiceCount(),
                roll.getResults(),
                total,
                roll.getSuccessThreshold(),
                successCount,
                failCount,
                roll.getSourcePresetName(),
                roll.getRerollSourceRollId(),
                roll.getRerollType(),
                roll.getCreatedAt()
        );
    }
}