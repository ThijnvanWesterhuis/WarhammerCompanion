package backend.api.dto;

import backend.domain.DiceType;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DiceRollRequestDto {

    @NotNull(message = "Dice type is required")
    private DiceType diceType;

    @NotNull(message = "Dice count is required")
    @Min(value = 1, message = "Dice count must be at least 1")
    @Max(value = 100, message = "Dice count may not be higher than 100")
    private Integer diceCount;

    // Optional: used to remember which preset caused the roll
    private Long presetId;
}