package backend.api.dto;

import backend.domain.DicePreset;
import backend.domain.DiceType;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class DicePresetResponseDto {
    private Long id;
    private String name;
    private DiceType diceType;
    private Integer diceCount;
    private String phase;
    private LocalDateTime createdAt;

    public static DicePresetResponseDto fromPreset(DicePreset preset) {
        return new DicePresetResponseDto(
                preset.getId(),
                preset.getName(),
                preset.getDiceType(),
                preset.getDiceCount(),
                preset.getPhase(),
                preset.getCreatedAt()
        );
    }
}