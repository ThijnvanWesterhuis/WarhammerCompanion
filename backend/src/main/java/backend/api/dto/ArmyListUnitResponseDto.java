package backend.api.dto;

import backend.domain.ArmyListUnit;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ArmyListUnitResponseDto {
    private Long id;
    private String name;
    private String unitType;
    private Integer points;
    private Integer quantity;
    private Integer totalPoints;
    private String notes;

    public static ArmyListUnitResponseDto fromUnit(ArmyListUnit unit) {
        int points = unit.getPoints() == null ? 0 : unit.getPoints();
        int quantity = unit.getQuantity() == null ? 1 : unit.getQuantity();

        return new ArmyListUnitResponseDto(
                unit.getId(),
                unit.getName(),
                unit.getUnitType(),
                points,
                quantity,
                points * quantity,
                unit.getNotes()
        );
    }
}