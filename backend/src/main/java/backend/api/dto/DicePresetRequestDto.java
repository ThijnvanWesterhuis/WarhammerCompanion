package backend.api.dto;

import backend.domain.DiceType;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DicePresetRequestDto {

    @NotBlank(message = "Preset name is required")
    @Size(max = 80, message = "Preset name may not be longer than 80 characters")
    private String name;

    @NotNull(message = "Dice type is required")
    private DiceType diceType;

    @NotNull(message = "Dice count is required")
    @Min(value = 1, message = "Dice count must be at least 1")
    @Max(value = 100, message = "Dice count may not be higher than 100")
    private Integer diceCount;

    @Size(max = 50, message = "Phase may not be longer than 50 characters")
    private String phase;
}