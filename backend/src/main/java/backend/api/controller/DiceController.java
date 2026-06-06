package backend.api.controller;

import backend.api.dto.*;
import backend.application.service.DiceService;
import backend.domain.User;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/dice")
@RequiredArgsConstructor
public class DiceController {

    private final DiceService diceService;

    @GetMapping("/presets")
    public ResponseEntity<List<DicePresetResponseDto>> getPresets(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(diceService.getPresets(user));
    }

    @PostMapping("/presets")
    public ResponseEntity<DicePresetResponseDto> createPreset(
            @AuthenticationPrincipal User user,
            @Valid @RequestBody DicePresetRequestDto request
    ) {
        return ResponseEntity.ok(diceService.createPreset(user, request));
    }

    @PutMapping("/presets/{presetId}")
    public ResponseEntity<DicePresetResponseDto> updatePreset(
            @AuthenticationPrincipal User user,
            @PathVariable Long presetId,
            @Valid @RequestBody DicePresetRequestDto request
    ) {
        return ResponseEntity.ok(diceService.updatePreset(user, presetId, request));
    }

    @DeleteMapping("/presets/{presetId}")
    public ResponseEntity<Void> deletePreset(
            @AuthenticationPrincipal User user,
            @PathVariable Long presetId
    ) {
        diceService.deletePreset(user, presetId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/roll")
    public ResponseEntity<DiceRollResponseDto> rollDice(
            @AuthenticationPrincipal User user,
            @Valid @RequestBody DiceRollRequestDto request
    ) {
        return ResponseEntity.ok(diceService.rollDice(user, request));
    }

    @GetMapping("/rolls")
    public ResponseEntity<List<DiceRollResponseDto>> getRollHistory(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(diceService.getRollHistory(user));
    }
}