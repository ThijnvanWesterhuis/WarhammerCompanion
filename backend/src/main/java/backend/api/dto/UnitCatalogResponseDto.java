package backend.api.dto;

import backend.domain.UnitCatalog;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class UnitCatalogResponseDto {
    private Long id;
    private String editionCode;
    private String faction;
    private String name;
    private String unitType;
    private Integer points;
    private String models;
    private String keywords;

    public static UnitCatalogResponseDto fromUnitCatalog(UnitCatalog unit) {
        return new UnitCatalogResponseDto(
                unit.getId(),
                unit.getGameEdition().getCode(),
                unit.getFaction(),
                unit.getName(),
                unit.getUnitType(),
                unit.getPoints(),
                unit.getModels(),
                unit.getKeywords()
        );
    }
}