package backend.api.controller;

import backend.api.dto.*;
import backend.application.service.GameSessionService;
import backend.domain.User;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/sessions")
@RequiredArgsConstructor
public class GameSessionController {

    private final GameSessionService gameSessionService;

    @PostMapping("/start")
    public ResponseEntity<GameSessionResponseDto> startSession(
            @AuthenticationPrincipal User user,
            @Valid @RequestBody GameSessionRequestDto request
    ) {
        return ResponseEntity.ok(gameSessionService.startSession(user, request));
    }

    @GetMapping
    public ResponseEntity<List<GameSessionResponseDto>> getSessions(
            @AuthenticationPrincipal User user
    ) {
        return ResponseEntity.ok(gameSessionService.getSessions(user));
    }

    @GetMapping("/history")
    public ResponseEntity<PageResponseDto<GameSessionResponseDto>> getMatchHistory(
            @AuthenticationPrincipal User user,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String faction,
            @RequestParam(required = false) String result
    ) {
        return ResponseEntity.ok(gameSessionService.getMatchHistory(
                user,
                page,
                size,
                search,
                faction,
                result
        ));
    }

    @GetMapping("/active")
    public ResponseEntity<GameSessionResponseDto> getActiveSession(
            @AuthenticationPrincipal User user
    ) {
        return ResponseEntity.ok(gameSessionService.getActiveSession(user));
    }

    @GetMapping("/{sessionId}")
    public ResponseEntity<GameSessionResponseDto> getSession(
            @AuthenticationPrincipal User user,
            @PathVariable Long sessionId
    ) {
        return ResponseEntity.ok(gameSessionService.getSession(user, sessionId));
    }

    @PatchMapping("/{sessionId}/score")
    public ResponseEntity<GameSessionResponseDto> updateScore(
            @AuthenticationPrincipal User user,
            @PathVariable Long sessionId,
            @Valid @RequestBody GameSessionScoreRequestDto request
    ) {
        return ResponseEntity.ok(gameSessionService.updateScore(user, sessionId, request));
    }

    @PatchMapping("/{sessionId}/round")
    public ResponseEntity<GameSessionResponseDto> updateRound(
            @AuthenticationPrincipal User user,
            @PathVariable Long sessionId,
            @Valid @RequestBody GameSessionRoundRequestDto request
    ) {
        return ResponseEntity.ok(gameSessionService.updateRound(user, sessionId, request));
    }

    @PatchMapping("/{sessionId}/end")
    public ResponseEntity<GameSessionResponseDto> endSession(
            @AuthenticationPrincipal User user,
            @PathVariable Long sessionId
    ) {
        return ResponseEntity.ok(gameSessionService.endSession(user, sessionId));
    }

    @PatchMapping("/{sessionId}")
    public ResponseEntity<GameSessionResponseDto> updateSavedMatch(
            @AuthenticationPrincipal User user,
            @PathVariable Long sessionId,
            @Valid @RequestBody GameSessionUpdateRequestDto request
    ) {
        return ResponseEntity.ok(gameSessionService.updateSavedMatch(user, sessionId, request));
    }

    @DeleteMapping("/{sessionId}")
    public ResponseEntity<Void> deleteSavedMatch(
            @AuthenticationPrincipal User user,
            @PathVariable Long sessionId
    ) {
        gameSessionService.deleteSavedMatch(user, sessionId);
        return ResponseEntity.noContent().build();
    }
}