package backend.api.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ArmyListUnitRequestDto {
    @NotBlank(message = "Unit name is required")
    @Size(max = 120, message = "Unit name can contain at most 120 characters")
    private String name;

    @Size(max = 80, message = "Unit type can contain at most 80 characters")
    private String unitType;

    @Min(value = 0, message = "Points cannot be negative")
    private Integer points = 0;

    @Min(value = 1, message = "Quantity must be at least 1")
    private Integer quantity = 1;

    @Size(max = 500, message = "Unit notes can contain at most 500 characters")
    private String notes;
}