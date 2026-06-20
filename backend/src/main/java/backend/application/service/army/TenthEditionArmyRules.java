package backend.application.service.army;

import backend.api.dto.ArmyListRequestDto;
import backend.api.dto.ArmyListUnitRequestDto;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

@Component
public class TenthEditionArmyRules implements ArmyEditionRules {
    private static final int MAX_UNITS_PER_DATASHEET = 3;
    private static final int MAX_BATTLELINE_OR_TRANSPORT_PER_DATASHEET = 6;
    private static final Set<Integer> SUPPORTED_POINTS_LIMITS = Set.of(500, 1000, 2000);

    @Override
    public String getEditionCode() {
        return "10TH";
    }

    @Override
    public void validate(ArmyListRequestDto request, int totalPoints) {
        validateBasicArmyList(request, totalPoints);
        validateDetachmentSelected(request);
        validateCharacterRequirement(request);
        validateDatasheetLimits(request);
    }

    private void validateBasicArmyList(ArmyListRequestDto request, int totalPoints) {
        int pointsLimit = request.getPointsLimit() == null ? 0 : request.getPointsLimit();

        if (!SUPPORTED_POINTS_LIMITS.contains(pointsLimit)) {
            throw new IllegalArgumentException("10th edition army lists must use a 500, 1000 or 2000 point limit");
        }

        if (totalPoints > pointsLimit) {
            throw new IllegalArgumentException("10th edition army list cannot exceed the points limit");
        }

        if (request.getUnits() == null || request.getUnits().isEmpty()) {
            throw new IllegalArgumentException("10th edition army list must contain at least one unit");
        }
    }

    private void validateDetachmentSelected(ArmyListRequestDto request) {
        if (isBlank(request.getArmyRule())) {
            throw new IllegalArgumentException("10th edition army list must have a detachment selected");
        }
    }

    private void validateCharacterRequirement(ArmyListRequestDto request) {
        boolean hasCharacter = request.getUnits()
                .stream()
                .anyMatch(this::isCharacterUnit);

        if (!hasCharacter) {
            throw new IllegalArgumentException("10th edition army list must contain at least one Character unit");
        }
    }

    private void validateDatasheetLimits(ArmyListRequestDto request) {
        Map<String, Integer> datasheetCounts = new HashMap<>();
        Map<String, ArmyListUnitRequestDto> firstUnitByDatasheet = new HashMap<>();

        for (ArmyListUnitRequestDto unit : request.getUnits()) {
            String datasheetName = normalize(unit.getName());
            int quantity = safeQuantity(unit.getQuantity());

            datasheetCounts.merge(datasheetName, quantity, Integer::sum);
            firstUnitByDatasheet.putIfAbsent(datasheetName, unit);
        }

        for (Map.Entry<String, Integer> entry : datasheetCounts.entrySet()) {
            ArmyListUnitRequestDto unit = firstUnitByDatasheet.get(entry.getKey());
            int maximumAllowed = maximumAllowedCopies(unit);

            if (entry.getValue() > maximumAllowed) {
                throw new IllegalArgumentException(
                        unit.getName() + " exceeds the 10th edition datasheet limit of " + maximumAllowed
                );
            }
        }
    }

    private int maximumAllowedCopies(ArmyListUnitRequestDto unit) {
        if (isEpicHero(unit)) {
            return 1;
        }

        if (hasUnitType(unit, "Battleline") || hasUnitType(unit, "Dedicated Transport")) {
            return MAX_BATTLELINE_OR_TRANSPORT_PER_DATASHEET;
        }

        return MAX_UNITS_PER_DATASHEET;
    }

    private boolean isCharacterUnit(ArmyListUnitRequestDto unit) {
        return hasUnitType(unit, "Character") || isEpicHero(unit) || containsKeyword(unit, "Character");
    }

    private boolean isEpicHero(ArmyListUnitRequestDto unit) {
        return hasUnitType(unit, "Epic Hero") || containsKeyword(unit, "Epic Hero");
    }

    private boolean hasUnitType(ArmyListUnitRequestDto unit, String expectedUnitType) {
        return normalize(unit.getUnitType()).equals(normalize(expectedUnitType));
    }

    private boolean containsKeyword(ArmyListUnitRequestDto unit, String expectedKeyword) {
        return normalize(unit.getNotes()).contains(normalize(expectedKeyword));
    }

    private int safeQuantity(Integer quantity) {
        return quantity == null || quantity < 1 ? 1 : quantity;
    }

    private String normalize(String value) {
        return value == null ? "" : value.trim().toLowerCase(Locale.ROOT);
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isBlank();
    }
}