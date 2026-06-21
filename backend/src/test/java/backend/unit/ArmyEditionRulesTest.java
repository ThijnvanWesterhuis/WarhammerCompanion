package backend.unit;

import backend.api.dto.ArmyListRequestDto;
import backend.api.dto.ArmyListUnitRequestDto;
import backend.application.service.army.NinthEditionArmyRules;
import backend.application.service.army.TenthEditionArmyRules;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ArmyEditionRulesTest {

    @Test
    void tenthEditionRejectsListOverPointsLimit() {
        ArmyListRequestDto request = tenthEditionRequest(2000, units(
                unit("Captain", "Character", 80, 1, "Character"),
                unit("Intercessor Squad", "Battleline", 80, 1, "Infantry")
        ));

        TenthEditionArmyRules rules = new TenthEditionArmyRules();

        assertThrows(IllegalArgumentException.class, () -> rules.validate(request, 2001));
    }

    @Test
    void tenthEditionRequiresAtLeastOneCharacter() {
        ArmyListRequestDto request = tenthEditionRequest(2000, units(
                unit("Intercessor Squad", "Battleline", 80, 1, "Infantry")
        ));

        TenthEditionArmyRules rules = new TenthEditionArmyRules();

        assertThrows(IllegalArgumentException.class, () -> rules.validate(request, 80));
    }

    @Test
    void tenthEditionLimitsNormalDatasheetsToThreeCopies() {
        ArmyListRequestDto request = tenthEditionRequest(2000, units(
                unit("Captain", "Character", 80, 1, "Character"),
                unit("Terminator Squad", "Infantry", 170, 4, "Infantry")
        ));

        TenthEditionArmyRules rules = new TenthEditionArmyRules();

        assertThrows(IllegalArgumentException.class, () -> rules.validate(request, 760));
    }

    @Test
    void tenthEditionAllowsBattlelineUpToSixCopies() {
        ArmyListRequestDto request = tenthEditionRequest(2000, units(
                unit("Captain", "Character", 80, 1, "Character"),
                unit("Intercessor Squad", "Battleline", 80, 6, "Infantry")
        ));

        TenthEditionArmyRules rules = new TenthEditionArmyRules();

        assertDoesNotThrow(() -> rules.validate(request, 560));
    }

    @Test
    void tenthEditionRejectsDuplicateEpicHero() {
        ArmyListRequestDto request = tenthEditionRequest(2000, units(
                unit("Captain", "Character", 80, 1, "Character"),
                unit("Marneus Calgar", "Epic Hero", 200, 2, "Character, Infantry")
        ));

        TenthEditionArmyRules rules = new TenthEditionArmyRules();

        assertThrows(IllegalArgumentException.class, () -> rules.validate(request, 480));
    }

    @Test
    void ninthEditionAcceptsValidBattalionDetachment() {
        ArmyListRequestDto request = ninthEditionRequest("Battalion Detachment", 1000, units(
                unit("Primaris Captain", "HQ", 80, 1, "Character"),
                unit("Primaris Lieutenant", "HQ", 65, 1, "Character"),
                unit("Intercessor Squad", "Troops", 90, 1, "Infantry"),
                unit("Assault Intercessor Squad", "Troops", 85, 1, "Infantry"),
                unit("Tactical Squad", "Troops", 90, 1, "Infantry")
        ));

        NinthEditionArmyRules rules = new NinthEditionArmyRules();

        assertDoesNotThrow(() -> rules.validate(request, 410));
    }

    @Test
    void ninthEditionRejectsBattalionWithoutEnoughTroops() {
        ArmyListRequestDto request = ninthEditionRequest("Battalion Detachment", 1000, units(
                unit("Primaris Captain", "HQ", 80, 1, "Character"),
                unit("Primaris Lieutenant", "HQ", 65, 1, "Character"),
                unit("Intercessor Squad", "Troops", 90, 1, "Infantry")
        ));

        NinthEditionArmyRules rules = new NinthEditionArmyRules();

        assertThrows(IllegalArgumentException.class, () -> rules.validate(request, 235));
    }

    @Test
    void ninthEditionRejectsUnsupportedDetachment() {
        ArmyListRequestDto request = ninthEditionRequest("Gladius Task Force", 1000, units(
                unit("Primaris Captain", "HQ", 80, 1, "Character"),
                unit("Intercessor Squad", "Troops", 90, 1, "Infantry")
        ));

        NinthEditionArmyRules rules = new NinthEditionArmyRules();

        assertThrows(IllegalArgumentException.class, () -> rules.validate(request, 170));
    }

    @Test
    void ninthEditionRejectsInvalidBattlefieldRole() {
        ArmyListRequestDto request = ninthEditionRequest("Patrol Detachment", 1000, units(
                unit("Primaris Captain", "Character", 80, 1, "Character"),
                unit("Intercessor Squad", "Battleline", 90, 1, "Infantry")
        ));

        NinthEditionArmyRules rules = new NinthEditionArmyRules();

        assertThrows(IllegalArgumentException.class, () -> rules.validate(request, 170));
    }

    private ArmyListRequestDto tenthEditionRequest(int pointsLimit, List<ArmyListUnitRequestDto> units) {
        ArmyListRequestDto request = baseRequest(pointsLimit, units);
        request.setGameEditionCode("10TH");
        request.setArmyRule("Gladius Task Force");
        return request;
    }

    private ArmyListRequestDto ninthEditionRequest(
            String detachment,
            int pointsLimit,
            List<ArmyListUnitRequestDto> units
    ) {
        ArmyListRequestDto request = baseRequest(pointsLimit, units);
        request.setGameEditionCode("9TH");
        request.setArmyRule(detachment);
        return request;
    }

    private ArmyListRequestDto baseRequest(int pointsLimit, List<ArmyListUnitRequestDto> units) {
        ArmyListRequestDto request = new ArmyListRequestDto();
        request.setName("Test List");
        request.setFaction("Ultramarines");
        request.setPointsLimit(pointsLimit);
        request.setDescription("Test army list");
        request.setUnits(units);
        return request;
    }

    private List<ArmyListUnitRequestDto> units(ArmyListUnitRequestDto... units) {
        return new ArrayList<>(List.of(units));
    }

    private ArmyListUnitRequestDto unit(
            String name,
            String unitType,
            int points,
            int quantity,
            String notes
    ) {
        ArmyListUnitRequestDto unit = new ArmyListUnitRequestDto();
        unit.setName(name);
        unit.setUnitType(unitType);
        unit.setPoints(points);
        unit.setQuantity(quantity);
        unit.setNotes(notes);
        return unit;
    }
}