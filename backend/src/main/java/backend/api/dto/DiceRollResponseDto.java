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
    private DiceType diceType;
    private Integer diceCount;
    private List<Integer> results;
    private Integer total;
    private String sourcePresetName;
    private LocalDateTime createdAt;

    public static DiceRollResponseDto fromRoll(DiceRoll roll) {
        int total = roll.getResults().stream()
                .mapToInt(Integer::intValue)
                .sum();

        return new DiceRollResponseDto(
                roll.getId(),
                roll.getDiceType(),
                roll.getDiceCount(),
                roll.getResults(),
                total,
                roll.getSourcePresetName(),
                roll.getCreatedAt()
        );
    }
}