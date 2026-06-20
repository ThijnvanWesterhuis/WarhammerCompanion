package backend.api.dto;

import backend.domain.GameEdition;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class GameEditionResponseDto {
    private Long id;
    private String code;
    private String displayName;
    private String description;
    private Integer releaseOrder;

    public static GameEditionResponseDto fromEdition(GameEdition edition) {
        return new GameEditionResponseDto(
                edition.getId(),
                edition.getCode(),
                edition.getDisplayName(),
                edition.getDescription(),
                edition.getReleaseOrder()
        );
    }
}