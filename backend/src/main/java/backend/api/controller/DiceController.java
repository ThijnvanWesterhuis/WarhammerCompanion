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

    @PostMapping("/reroll-last")
    public ResponseEntity<DiceRollResponseDto> rerollLastRoll(
            @AuthenticationPrincipal User user,
            @RequestParam(required = false) Long sessionId
    ) {
        return ResponseEntity.ok(diceService.rerollLastRoll(user, sessionId));
    }

    @PostMapping("/reroll-value")
    public ResponseEntity<DiceRollResponseDto> rerollDiceWithValue(
            @AuthenticationPrincipal User user,
            @Valid @RequestBody DiceRerollValueRequestDto request
    ) {
        return ResponseEntity.ok(diceService.rerollDiceWithValue(user, request));
    }

    @GetMapping("/rolls")
    public ResponseEntity<List<DiceRollResponseDto>> getRollHistory(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(diceService.getRollHistory(user));
    }

    @GetMapping("/rolls/session/{sessionId}")
    public ResponseEntity<List<DiceRollResponseDto>> getSessionRollHistory(
            @AuthenticationPrincipal User user,
            @PathVariable Long sessionId
    ) {
        return ResponseEntity.ok(diceService.getSessionRollHistory(user, sessionId));
    }
}