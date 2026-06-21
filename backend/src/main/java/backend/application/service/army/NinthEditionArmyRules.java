package backend.application.service.army;

import backend.api.dto.ArmyListRequestDto;
import backend.api.dto.ArmyListUnitRequestDto;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

@Component
public class NinthEditionArmyRules implements ArmyEditionRules {
    private static final Set<Integer> SUPPORTED_POINTS_LIMITS = Set.of(500, 1000, 2000);

    private static final Set<String> RECOGNIZED_BATTLEFIELD_ROLES = Set.of(
            "hq",
            "troops",
            "elites",
            "fast attack",
            "heavy support",
            "flyer",
            "lord of war"
    );

    private static final Map<String, DetachmentLimits> DETACHMENT_LIMITS = Map.of(
            "patrol detachment", new DetachmentLimits(
                    1, 2,
                    1, 3,
                    0, 2,
                    0, 2,
                    0, 2,
                    0, 2,
                    0, 0
            ),
            "battalion detachment", new DetachmentLimits(
                    2, 3,
                    3, 6,
                    0, 6,
                    0, 3,
                    0, 3,
                    0, 2,
                    0, 0
            ),
            "brigade detachment", new DetachmentLimits(
                    3, 5,
                    6, 12,
                    3, 8,
                    3, 5,
                    3, 5,
                    0, 2,
                    0, 0
            ),
            "vanguard detachment", new DetachmentLimits(
                    1, 2,
                    0, 3,
                    3, 6,
                    0, 2,
                    0, 2,
                    0, 2,
                    0, 0
            ),
            "spearhead detachment", new DetachmentLimits(
                    1, 2,
                    0, 3,
                    0, 2,
                    0, 2,
                    3, 6,
                    0, 2,
                    0, 0
            ),
            "outrider detachment", new DetachmentLimits(
                    1, 2,
                    0, 3,
                    0, 2,
                    3, 6,
                    0, 2,
                    0, 2,
                    0, 0
            ),
            "super-heavy auxiliary detachment", new DetachmentLimits(
                    0, 0,
                    0, 0,
                    0, 0,
                    0, 0,
                    0, 0,
                    0, 0,
                    1, 1
            )
    );

    @Override
    public String getEditionCode() {
        return "9TH";
    }

    @Override
    public void validate(ArmyListRequestDto request, int totalPoints) {
        validateBasicArmyList(request, totalPoints);
        DetachmentLimits limits = validateAndGetDetachmentLimits(request);
        validateBattlefieldRoleSlots(request, limits);
    }

    private void validateBasicArmyList(ArmyListRequestDto request, int totalPoints) {
        int pointsLimit = request.getPointsLimit() == null ? 0 : request.getPointsLimit();

        if (!SUPPORTED_POINTS_LIMITS.contains(pointsLimit)) {
            throw new IllegalArgumentException("9th edition army lists must use a 500, 1000 or 2000 point limit");
        }

        if (totalPoints > pointsLimit) {
            throw new IllegalArgumentException("9th edition army list cannot exceed the points limit");
        }

        if (request.getUnits() == null || request.getUnits().isEmpty()) {
            throw new IllegalArgumentException("9th edition army list must contain at least one unit");
        }
    }

    private DetachmentLimits validateAndGetDetachmentLimits(ArmyListRequestDto request) {
        String detachment = normalize(request.getArmyRule());

        if (detachment.isBlank()) {
            throw new IllegalArgumentException("9th edition army list must have a detachment selected");
        }

        DetachmentLimits limits = DETACHMENT_LIMITS.get(detachment);

        if (limits == null) {
            throw new IllegalArgumentException("Unsupported 9th edition detachment: " + request.getArmyRule());
        }

        return limits;
    }

    private void validateBattlefieldRoleSlots(ArmyListRequestDto request, DetachmentLimits limits) {
        Map<String, Integer> roleCounts = new HashMap<>();

        for (ArmyListUnitRequestDto unit : request.getUnits()) {
            String role = normalize(unit.getUnitType());

            if (!RECOGNIZED_BATTLEFIELD_ROLES.contains(role)) {
                throw new IllegalArgumentException(
                        unit.getName() + " must have a valid 9th edition battlefield role"
                );
            }

            roleCounts.merge(role, safeQuantity(unit.getQuantity()), Integer::sum);
        }

        validateRange("HQ", roleCounts.getOrDefault("hq", 0), limits.minHq(), limits.maxHq());
        validateRange("Troops", roleCounts.getOrDefault("troops", 0), limits.minTroops(), limits.maxTroops());
        validateRange("Elites", roleCounts.getOrDefault("elites", 0), limits.minElites(), limits.maxElites());
        validateRange("Fast Attack", roleCounts.getOrDefault("fast attack", 0), limits.minFastAttack(), limits.maxFastAttack());
        validateRange("Heavy Support", roleCounts.getOrDefault("heavy support", 0), limits.minHeavySupport(), limits.maxHeavySupport());
        validateRange("Flyer", roleCounts.getOrDefault("flyer", 0), limits.minFlyer(), limits.maxFlyer());
        validateRange("Lord of War", roleCounts.getOrDefault("lord of war", 0), limits.minLordOfWar(), limits.maxLordOfWar());
    }

    private void validateRange(String role, int count, int minimum, int maximum) {
        if (count < minimum || count > maximum) {
            throw new IllegalArgumentException(
                    role + " slot count must be between " + minimum + " and " + maximum + " for the selected 9th edition detachment"
            );
        }
    }

    private int safeQuantity(Integer quantity) {
        return quantity == null || quantity < 1 ? 1 : quantity;
    }

    private String normalize(String value) {
        return value == null ? "" : value.trim().toLowerCase(Locale.ROOT);
    }

    private record DetachmentLimits(
            int minHq,
            int maxHq,
            int minTroops,
            int maxTroops,
            int minElites,
            int maxElites,
            int minFastAttack,
            int maxFastAttack,
            int minHeavySupport,
            int maxHeavySupport,
            int minFlyer,
            int maxFlyer,
            int minLordOfWar,
            int maxLordOfWar
    ) {
    }
}