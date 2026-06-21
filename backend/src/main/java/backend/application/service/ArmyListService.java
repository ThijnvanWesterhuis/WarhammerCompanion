package backend.application.service;

import backend.api.dto.ArmyListRequestDto;
import backend.api.dto.ArmyListResponseDto;
import backend.api.dto.GameEditionResponseDto;
import backend.api.dto.PageResponseDto;
import backend.application.service.army.ArmyEditionRulesFactory;
import backend.data.repository.ArmyListRepository;
import backend.data.repository.GameEditionRepository;
import backend.domain.ArmyList;
import backend.domain.ArmyListUnit;
import backend.domain.GameEdition;
import backend.domain.User;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ArmyListService {

    private final ArmyListRepository armyListRepository;
    private final GameEditionRepository gameEditionRepository;
    private final ArmyEditionRulesFactory rulesFactory;

    @Transactional(readOnly = true)
    public List<GameEditionResponseDto> getSupportedEditions() {
        return gameEditionRepository.findAllByOrderByReleaseOrderDesc()
                .stream()
                .map(GameEditionResponseDto::fromEdition)
                .toList();
    }

    @Transactional(readOnly = true)
    public PageResponseDto<ArmyListResponseDto> getArmyLists(
            User user,
            int page,
            int size,
            String search,
            String faction,
            String editionCode
    ) {
        int safePage = Math.max(page, 0);
        int safeSize = Math.min(Math.max(size, 1), 50);
        Pageable pageable = PageRequest.of(safePage, safeSize);

        Page<ArmyList> armyListPage = armyListRepository.searchArmyLists(
                user,
                trimOrEmpty(search),
                trimOrEmpty(faction),
                normalizeEditionCodeOrEmpty(editionCode),
                pageable
        );

        List<ArmyListResponseDto> content = armyListPage.getContent()
                .stream()
                .map(ArmyListResponseDto::fromArmyList)
                .toList();

        return new PageResponseDto<>(
                content,
                armyListPage.getNumber(),
                armyListPage.getSize(),
                armyListPage.getTotalElements(),
                armyListPage.getTotalPages(),
                armyListPage.isFirst(),
                armyListPage.isLast()
        );
    }

    @Transactional(readOnly = true)
    public ArmyListResponseDto getArmyList(User user, Long armyListId) {
        return ArmyListResponseDto.fromArmyList(findArmyListForUser(user, armyListId));
    }

    @Transactional
    public ArmyListResponseDto createArmyList(User user, ArmyListRequestDto request) {
        String editionCode = normalizeEditionCode(request.getGameEditionCode());
        GameEdition edition = findEdition(editionCode);
        int totalPoints = calculateTotalPoints(request);

        rulesFactory.getRulesForEdition(editionCode).validate(request, totalPoints);

        ArmyList armyList = ArmyList.builder()
                .user(user)
                .gameEdition(edition)
                .name(trim(request.getName()))
                .faction(trim(request.getFaction()))
                .armyRule(trimOrNull(request.getArmyRule()))
                .pointsLimit(request.getPointsLimit())
                .totalPoints(totalPoints)
                .description(trimOrNull(request.getDescription()))
                .build();

        armyList.replaceUnits(toUnits(request));

        return ArmyListResponseDto.fromArmyList(armyListRepository.save(armyList));
    }

    @Transactional
    public ArmyListResponseDto updateArmyList(User user, Long armyListId, ArmyListRequestDto request) {
        ArmyList armyList = findArmyListForUser(user, armyListId);

        String editionCode = normalizeEditionCode(request.getGameEditionCode());
        GameEdition edition = findEdition(editionCode);
        int totalPoints = calculateTotalPoints(request);

        rulesFactory.getRulesForEdition(editionCode).validate(request, totalPoints);

        armyList.setGameEdition(edition);
        armyList.setName(trim(request.getName()));
        armyList.setFaction(trim(request.getFaction()));
        armyList.setArmyRule(trimOrNull(request.getArmyRule()));
        armyList.setPointsLimit(request.getPointsLimit());
        armyList.setTotalPoints(totalPoints);
        armyList.setDescription(trimOrNull(request.getDescription()));
        armyList.replaceUnits(toUnits(request));

        return ArmyListResponseDto.fromArmyList(armyListRepository.save(armyList));
    }

    @Transactional
    public void deleteArmyList(User user, Long armyListId) {
        ArmyList armyList = findArmyListForUser(user, armyListId);
        armyListRepository.delete(armyList);
    }

    @Transactional
    public ArmyListResponseDto duplicateArmyList(User user, Long armyListId) {
        ArmyList original = findArmyListForUser(user, armyListId);

        ArmyList copy = ArmyList.builder()
                .user(user)
                .gameEdition(original.getGameEdition())
                .name(original.getName() + " Copy")
                .faction(original.getFaction())
                .armyRule(original.getArmyRule())
                .pointsLimit(original.getPointsLimit())
                .totalPoints(original.getTotalPoints())
                .description(original.getDescription())
                .build();

        List<ArmyListUnit> copiedUnits = original.getUnits()
                .stream()
                .map(unit -> ArmyListUnit.builder()
                        .name(unit.getName())
                        .unitType(unit.getUnitType())
                        .points(unit.getPoints())
                        .quantity(unit.getQuantity())
                        .notes(unit.getNotes())
                        .build())
                .toList();

        copy.replaceUnits(copiedUnits);

        return ArmyListResponseDto.fromArmyList(armyListRepository.save(copy));
    }

    private ArmyList findArmyListForUser(User user, Long armyListId) {
        return armyListRepository.findByIdAndUser(armyListId, user)
                .orElseThrow(() -> new IllegalArgumentException("Army list was not found"));
    }

    private GameEdition findEdition(String editionCode) {
        return gameEditionRepository.findByCode(editionCode)
                .orElseThrow(() -> new IllegalArgumentException("Edition was not found"));
    }

    private List<ArmyListUnit> toUnits(ArmyListRequestDto request) {
        if (request.getUnits() == null) {
            return List.of();
        }

        return request.getUnits()
                .stream()
                .map(unit -> ArmyListUnit.builder()
                        .name(trim(unit.getName()))
                        .unitType(trimOrNull(unit.getUnitType()))
                        .points(safePoints(unit.getPoints()))
                        .quantity(safeQuantity(unit.getQuantity()))
                        .notes(trimOrNull(unit.getNotes()))
                        .build())
                .toList();
    }

    private int calculateTotalPoints(ArmyListRequestDto request) {
        if (request.getUnits() == null) {
            return 0;
        }

        return request.getUnits()
                .stream()
                .mapToInt(unit -> safePoints(unit.getPoints()) * safeQuantity(unit.getQuantity()))
                .sum();
    }

    private int safePoints(Integer points) {
        return points == null || points < 0 ? 0 : points;
    }

    private int safeQuantity(Integer quantity) {
        return quantity == null || quantity < 1 ? 1 : quantity;
    }

    private String normalizeEditionCode(String editionCode) {
        String trimmedEditionCode = trim(editionCode).toUpperCase();

        if (!trimmedEditionCode.equals("10TH") && !trimmedEditionCode.equals("9TH")) {
            throw new IllegalArgumentException("Only 10th and 9th edition are supported right now");
        }

        return trimmedEditionCode;
    }

    private String normalizeEditionCodeOrEmpty(String editionCode) {
        if (editionCode == null || editionCode.trim().isBlank()) {
            return "";
        }

        return normalizeEditionCode(editionCode);
    }

    private String trim(String value) {
        if (value == null || value.trim().isBlank()) {
            return "";
        }

        return value.trim();
    }

    private String trimOrNull(String value) {
        if (value == null || value.trim().isBlank()) {
            return null;
        }

        return value.trim();
    }

    private String trimOrEmpty(String value) {
        if (value == null || value.trim().isBlank()) {
            return "";
        }

        return value.trim();
    }
}