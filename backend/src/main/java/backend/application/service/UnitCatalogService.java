package backend.application.service;

import backend.api.dto.UnitCatalogResponseDto;
import backend.data.repository.GameEditionRepository;
import backend.data.repository.UnitCatalogRepository;
import backend.domain.GameEdition;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UnitCatalogService {

    private final UnitCatalogRepository unitCatalogRepository;
    private final GameEditionRepository gameEditionRepository;

    @Transactional(readOnly = true)
    public List<UnitCatalogResponseDto> getUnits(String editionCode, String faction) {
        String normalizedEditionCode = normalizeEditionCode(editionCode);
        String normalizedFaction = trim(faction);

        if (normalizedFaction.isBlank()) {
            throw new IllegalArgumentException("Faction is required");
        }

        GameEdition edition = gameEditionRepository.findByCode(normalizedEditionCode)
                .orElseThrow(() -> new IllegalArgumentException("Edition was not found"));

        return unitCatalogRepository
                .findByGameEditionAndFactionIgnoreCaseOrderByNameAsc(edition, normalizedFaction)
                .stream()
                .map(UnitCatalogResponseDto::fromUnitCatalog)
                .toList();
    }

    private String normalizeEditionCode(String editionCode) {
        String trimmedEditionCode = trim(editionCode).toUpperCase();

        if (!trimmedEditionCode.equals("10TH") && !trimmedEditionCode.equals("9TH")) {
            throw new IllegalArgumentException("Only 10th and 9th edition are supported right now");
        }

        return trimmedEditionCode;
    }

    private String trim(String value) {
        if (value == null || value.trim().isBlank()) {
            return "";
        }

        return value.trim();
    }
}