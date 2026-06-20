package backend.api.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class ArmyListRequestDto {
    @NotBlank(message = "Army list name is required")
    @Size(max = 100, message = "Army list name can contain at most 100 characters")
    private String name;

    @NotBlank(message = "Edition is required")
    @Size(max = 20, message = "Edition code can contain at most 20 characters")
    private String gameEditionCode;

    @NotBlank(message = "Faction is required")
    @Size(max = 80, message = "Faction can contain at most 80 characters")
    private String faction;

    @Size(max = 80, message = "Army rule can contain at most 80 characters")
    private String armyRule;

    @Min(value = 1, message = "Points limit must be at least 1")
    private Integer pointsLimit = 2000;

    @Size(max = 1000, message = "Description can contain at most 1000 characters")
    private String description;

    @Valid
    private List<ArmyListUnitRequestDto> units = new ArrayList<>();
}