package backend.api.dto;

import backend.domain.ArmyList;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@AllArgsConstructor
public class ArmyListResponseDto {
    private Long id;
    private GameEditionResponseDto gameEdition;
    private String name;
    private String faction;
    private String armyRule;
    private Integer pointsLimit;
    private Integer totalPoints;
    private Integer remainingPoints;
    private String description;
    private List<ArmyListUnitResponseDto> units;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static ArmyListResponseDto fromArmyList(ArmyList armyList) {
        List<ArmyListUnitResponseDto> unitResponses = armyList.getUnits()
                .stream()
                .map(ArmyListUnitResponseDto::fromUnit)
                .toList();

        return new ArmyListResponseDto(
                armyList.getId(),
                GameEditionResponseDto.fromEdition(armyList.getGameEdition()),
                armyList.getName(),
                armyList.getFaction(),
                armyList.getArmyRule(),
                armyList.getPointsLimit(),
                armyList.getTotalPoints(),
                armyList.getPointsLimit() - armyList.getTotalPoints(),
                armyList.getDescription(),
                unitResponses,
                armyList.getCreatedAt(),
                armyList.getUpdatedAt()
        );
    }
}