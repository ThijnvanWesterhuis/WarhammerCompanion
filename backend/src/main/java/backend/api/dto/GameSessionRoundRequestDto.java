package backend.api.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GameSessionRoundRequestDto {

    @NotNull(message = "Current round is required")
    @Min(value = 1, message = "Round must be at least 1")
    @Max(value = 5, message = "Round may not be higher than 5")
    private Integer currentRound;
}