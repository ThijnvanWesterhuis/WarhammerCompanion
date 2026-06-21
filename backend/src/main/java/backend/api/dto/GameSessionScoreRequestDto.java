package backend.api.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GameSessionScoreRequestDto {

    @NotNull(message = "Player one score is required")
    @Min(value = 0, message = "Player one score may not be negative")
    private Integer playerOneScore;

    @NotNull(message = "Player two score is required")
    @Min(value = 0, message = "Player two score may not be negative")
    private Integer playerTwoScore;
}