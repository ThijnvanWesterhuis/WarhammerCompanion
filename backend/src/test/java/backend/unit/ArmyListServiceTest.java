package backend.unit;

import backend.api.dto.ArmyListRequestDto;
import backend.api.dto.ArmyListResponseDto;
import backend.api.dto.ArmyListUnitRequestDto;
import backend.api.dto.PageResponseDto;
import backend.application.service.ArmyListService;
import backend.application.service.army.ArmyEditionRules;
import backend.application.service.army.ArmyEditionRulesFactory;
import backend.data.repository.ArmyListRepository;
import backend.data.repository.GameEditionRepository;
import backend.domain.ArmyList;
import backend.domain.ArmyListUnit;
import backend.domain.GameEdition;
import backend.domain.Role;
import backend.domain.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ArmyListServiceTest {

    @Mock
    private ArmyListRepository armyListRepository;

    @Mock
    private GameEditionRepository gameEditionRepository;

    @Mock
    private ArmyEditionRulesFactory rulesFactory;

    @Mock
    private ArmyEditionRules armyEditionRules;

    @InjectMocks
    private ArmyListService armyListService;

    @Test
    void createArmyListCalculatesTotalPointsAndTrimsInputBeforeSaving() {
        User user = testUser();
        GameEdition tenthEdition = edition("10TH", "10th Edition");

        ArmyListRequestDto request = request(
                "  Ultramarines Strike Force  ",
                " 10th ",
                "  Ultramarines  ",
                "  Gladius Task Force  ",
                500,
                "  Small 10th edition test list  ",
                units(
                        unit("  Captain  ", "  Character  ", 80, 1, "  Character  "),
                        unit("  Intercessor Squad  ", "  Battleline  ", 80, 2, "  Infantry  ")
                )
        );

        when(gameEditionRepository.findByCode("10TH")).thenReturn(Optional.of(tenthEdition));
        when(rulesFactory.getRulesForEdition("10TH")).thenReturn(armyEditionRules);
        when(armyListRepository.save(any(ArmyList.class))).thenAnswer(invocation -> {
            ArmyList saved = invocation.getArgument(0);
            saved.setId(10L);
            return saved;
        });

        ArmyListResponseDto response = armyListService.createArmyList(user, request);

        ArgumentCaptor<ArmyList> armyListCaptor = ArgumentCaptor.forClass(ArmyList.class);
        verify(armyListRepository).save(armyListCaptor.capture());
        verify(armyEditionRules).validate(request, 240);

        ArmyList savedArmyList = armyListCaptor.getValue();

        assertEquals("Ultramarines Strike Force", savedArmyList.getName());
        assertEquals("Ultramarines", savedArmyList.getFaction());
        assertEquals("Gladius Task Force", savedArmyList.getArmyRule());
        assertEquals(500, savedArmyList.getPointsLimit());
        assertEquals(240, savedArmyList.getTotalPoints());
        assertEquals(2, savedArmyList.getUnits().size());
        assertSame(savedArmyList, savedArmyList.getUnits().get(0).getArmyList());

        assertEquals(240, response.getTotalPoints());
        assertEquals(260, response.getRemainingPoints());
        assertEquals("10TH", response.getGameEdition().getCode());
    }

    @Test
    void updateArmyListReplacesOldUnitsAndUsesNewEditionRules() {
        User user = testUser();
        GameEdition tenthEdition = edition("10TH", "10th Edition");
        GameEdition ninthEdition = edition("9TH", "9th Edition");

        ArmyList existing = ArmyList.builder()
                .id(20L)
                .user(user)
                .gameEdition(tenthEdition)
                .name("Old List")
                .faction("Ultramarines")
                .armyRule("Gladius Task Force")
                .pointsLimit(500)
                .totalPoints(80)
                .description("Old description")
                .build();

        existing.replaceUnits(List.of(
                ArmyListUnit.builder()
                        .name("Old Unit")
                        .unitType("Character")
                        .points(80)
                        .quantity(1)
                        .notes("Old")
                        .build()
        ));

        ArmyListRequestDto request = request(
                "9th Patrol",
                "9th",
                "Ultramarines",
                "Patrol Detachment",
                500,
                "Updated to 9th edition",
                units(
                        unit("Primaris Captain", "HQ", 80, 1, "Character"),
                        unit("Intercessor Squad", "Troops", 90, 1, "Infantry")
                )
        );

        when(armyListRepository.findByIdAndUser(20L, user)).thenReturn(Optional.of(existing));
        when(gameEditionRepository.findByCode("9TH")).thenReturn(Optional.of(ninthEdition));
        when(rulesFactory.getRulesForEdition("9TH")).thenReturn(armyEditionRules);
        when(armyListRepository.save(any(ArmyList.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ArmyListResponseDto response = armyListService.updateArmyList(user, 20L, request);

        verify(armyEditionRules).validate(request, 170);

        assertEquals("9th Patrol", existing.getName());
        assertEquals("Patrol Detachment", existing.getArmyRule());
        assertEquals("9TH", existing.getGameEdition().getCode());
        assertEquals(170, existing.getTotalPoints());
        assertEquals(2, existing.getUnits().size());
        assertEquals("Primaris Captain", existing.getUnits().get(0).getName());
        assertEquals("Intercessor Squad", existing.getUnits().get(1).getName());

        assertEquals(170, response.getTotalPoints());
        assertEquals(330, response.getRemainingPoints());
        assertEquals("9TH", response.getGameEdition().getCode());
    }

    @Test
    void duplicateArmyListCopiesUnitsAndAppendsCopyToName() {
        User user = testUser();
        GameEdition tenthEdition = edition("10TH", "10th Edition");

        ArmyList original = ArmyList.builder()
                .id(30L)
                .user(user)
                .gameEdition(tenthEdition)
                .name("Original List")
                .faction("Ultramarines")
                .armyRule("Gladius Task Force")
                .pointsLimit(500)
                .totalPoints(240)
                .description("Original description")
                .build();

        original.replaceUnits(List.of(
                ArmyListUnit.builder()
                        .name("Captain")
                        .unitType("Character")
                        .points(80)
                        .quantity(1)
                        .notes("Character")
                        .build(),
                ArmyListUnit.builder()
                        .name("Intercessor Squad")
                        .unitType("Battleline")
                        .points(80)
                        .quantity(2)
                        .notes("Infantry")
                        .build()
        ));

        when(armyListRepository.findByIdAndUser(30L, user)).thenReturn(Optional.of(original));
        when(armyListRepository.save(any(ArmyList.class))).thenAnswer(invocation -> {
            ArmyList copy = invocation.getArgument(0);
            copy.setId(31L);
            return copy;
        });

        ArmyListResponseDto response = armyListService.duplicateArmyList(user, 30L);

        ArgumentCaptor<ArmyList> copyCaptor = ArgumentCaptor.forClass(ArmyList.class);
        verify(armyListRepository).save(copyCaptor.capture());

        ArmyList copy = copyCaptor.getValue();

        assertEquals("Original List Copy", copy.getName());
        assertEquals("Ultramarines", copy.getFaction());
        assertEquals("10TH", copy.getGameEdition().getCode());
        assertEquals(240, copy.getTotalPoints());
        assertEquals(2, copy.getUnits().size());

        assertNotSame(original.getUnits().get(0), copy.getUnits().get(0));
        assertSame(copy, copy.getUnits().get(0).getArmyList());

        assertEquals("Original List Copy", response.getName());
        assertEquals(2, response.getUnits().size());
    }

    @Test
    void getArmyListsNormalizesSearchFiltersAndLimitsPageSize() {
        User user = testUser();
        GameEdition tenthEdition = edition("10TH", "10th Edition");

        ArmyList armyList = ArmyList.builder()
                .id(40L)
                .user(user)
                .gameEdition(tenthEdition)
                .name("Tyranid Invasion")
                .faction("Tyranids")
                .armyRule("Invasion Fleet")
                .pointsLimit(1000)
                .totalPoints(230)
                .description("Search result")
                .build();

        when(armyListRepository.searchArmyLists(
                eq(user),
                eq("Invasion"),
                eq("Tyranids"),
                eq("10TH"),
                any(Pageable.class)
        )).thenAnswer(invocation -> {
            Pageable pageable = invocation.getArgument(4);
            return new PageImpl<>(List.of(armyList), pageable, 1);
        });

        PageResponseDto<ArmyListResponseDto> response = armyListService.getArmyLists(
                user,
                -5,
                999,
                "  Invasion  ",
                "Tyranids",
                " 10th "
        );

        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        verify(armyListRepository).searchArmyLists(
                eq(user),
                eq("Invasion"),
                eq("Tyranids"),
                eq("10TH"),
                pageableCaptor.capture()
        );

        Pageable pageable = pageableCaptor.getValue();

        assertEquals(0, pageable.getPageNumber());
        assertEquals(50, pageable.getPageSize());
        assertEquals(1, response.getContent().size());
        assertEquals("Tyranid Invasion", response.getContent().get(0).getName());
    }

    @Test
    void createArmyListRejectsUnsupportedEditionBeforeSaving() {
        User user = testUser();

        ArmyListRequestDto request = request(
                "Old Rules List",
                "8TH",
                "Ultramarines",
                "Some Detachment",
                500,
                null,
                units(unit("Captain", "Character", 80, 1, "Character"))
        );

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> armyListService.createArmyList(user, request)
        );

        assertEquals("Only 10th and 9th edition are supported right now", exception.getMessage());
        verifyNoInteractions(gameEditionRepository);
        verify(armyListRepository, never()).save(any(ArmyList.class));
    }

    private ArmyListRequestDto request(
            String name,
            String editionCode,
            String faction,
            String armyRule,
            int pointsLimit,
            String description,
            List<ArmyListUnitRequestDto> units
    ) {
        ArmyListRequestDto request = new ArmyListRequestDto();
        request.setName(name);
        request.setGameEditionCode(editionCode);
        request.setFaction(faction);
        request.setArmyRule(armyRule);
        request.setPointsLimit(pointsLimit);
        request.setDescription(description);
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

    private GameEdition edition(String code, String displayName) {
        return GameEdition.builder()
                .id(code.equals("10TH") ? 10L : 9L)
                .code(code)
                .displayName(displayName)
                .description(displayName + " test edition")
                .releaseOrder(code.equals("10TH") ? 10 : 9)
                .build();
    }

    private User testUser() {
        return User.builder()
                .id(1L)
                .username("tester")
                .email("tester@example.com")
                .password("password")
                .role(Role.USER)
                .build();
    }
}