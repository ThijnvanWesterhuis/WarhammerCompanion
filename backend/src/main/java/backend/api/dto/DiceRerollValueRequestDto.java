package backend.api.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DiceRerollValueRequestDto {

    @NotNull(message = "Roll id is required")
    private Long rollId;

    @NotNull(message = "Reroll value is required")
    @Min(value = 1, message = "Reroll value must be at least 1")
    private Integer rerollValue;
}