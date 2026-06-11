package backend.api.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GameSessionUpdateRequestDto {

    @Size(max = 80, message = "Player one name may not be longer than 80 characters")
    private String playerOneName;

    @Size(max = 80, message = "Player two name may not be longer than 80 characters")
    private String playerTwoName;

    @Size(max = 80, message = "Player one faction may not be longer than 80 characters")
    private String playerOneFaction;

    @Size(max = 80, message = "Player two faction may not be longer than 80 characters")
    private String playerTwoFaction;

    @Size(max = 120, message = "Mission name may not be longer than 120 characters")
    private String missionName;

    @Size(max = 120, message = "Deployment map may not be longer than 120 characters")
    private String deploymentMap;

    @Size(max = 1000, message = "Notes may not be longer than 1000 characters")
    private String notes;

    @NotNull(message = "Player one score is required")
    @Min(value = 0, message = "Player one score may not be negative")
    private Integer playerOneScore;

    @NotNull(message = "Player two score is required")
    @Min(value = 0, message = "Player two score may not be negative")
    private Integer playerTwoScore;
}