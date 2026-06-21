package backend.api.controller;

import backend.api.dto.ArmyListRequestDto;
import backend.api.dto.ArmyListResponseDto;
import backend.api.dto.GameEditionResponseDto;
import backend.api.dto.PageResponseDto;
import backend.application.service.ArmyListService;
import backend.domain.User;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/army-lists")
@RequiredArgsConstructor
public class ArmyListController {

    private final ArmyListService armyListService;

    @GetMapping("/editions")
    public List<GameEditionResponseDto> getSupportedEditions() {
        return armyListService.getSupportedEditions();
    }

    @GetMapping
    public PageResponseDto<ArmyListResponseDto> getArmyLists(
            @AuthenticationPrincipal User user,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "") String search,
            @RequestParam(defaultValue = "") String faction,
            @RequestParam(defaultValue = "") String editionCode
    ) {
        return armyListService.getArmyLists(user, page, size, search, faction, editionCode);
    }

    @GetMapping("/{id}")
    public ArmyListResponseDto getArmyList(
            @AuthenticationPrincipal User user,
            @PathVariable Long id
    ) {
        return armyListService.getArmyList(user, id);
    }

    @PostMapping
    public ArmyListResponseDto createArmyList(
            @AuthenticationPrincipal User user,
            @Valid @RequestBody ArmyListRequestDto request
    ) {
        return armyListService.createArmyList(user, request);
    }

    @PutMapping("/{id}")
    public ArmyListResponseDto updateArmyList(
            @AuthenticationPrincipal User user,
            @PathVariable Long id,
            @Valid @RequestBody ArmyListRequestDto request
    ) {
        return armyListService.updateArmyList(user, id, request);
    }

    @PostMapping("/{id}/duplicate")
    public ArmyListResponseDto duplicateArmyList(
            @AuthenticationPrincipal User user,
            @PathVariable Long id
    ) {
        return armyListService.duplicateArmyList(user, id);
    }

    @DeleteMapping("/{id}")
    public void deleteArmyList(
            @AuthenticationPrincipal User user,
            @PathVariable Long id
    ) {
        armyListService.deleteArmyList(user, id);
    }
}