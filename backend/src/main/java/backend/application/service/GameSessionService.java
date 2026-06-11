package backend.application.service;

import backend.api.dto.*;
import backend.api.event.SessionStartedEvent;
import backend.api.event.SessionUpdatedEvent;
import backend.data.repository.DiceRollRepository;
import backend.data.repository.GameSessionRepository;
import backend.data.repository.GameSessionRoundScoreRepository;
import backend.domain.GameSession;
import backend.domain.GameSessionRoundScore;
import backend.domain.GameSessionStatus;
import backend.domain.User;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class GameSessionService {

    private final GameSessionRepository gameSessionRepository;
    private final GameSessionRoundScoreRepository roundScoreRepository;
    private final DiceRollRepository diceRollRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public GameSessionResponseDto startSession(User user, GameSessionRequestDto request) {
        GameSession session = GameSession.builder()
                .user(user)
                .playerOneName(trimOrNull(request.getPlayerOneName()))
                .playerTwoName(trimOrNull(request.getPlayerTwoName()))
                .playerOneFaction(trimOrNull(request.getPlayerOneFaction()))
                .playerTwoFaction(trimOrNull(request.getPlayerTwoFaction()))
                .missionName(trimOrNull(request.getMissionName()))
                .deploymentMap(trimOrNull(request.getDeploymentMap()))
                .notes(trimOrNull(request.getNotes()))
                .currentRound(1)
                .playerOneScore(0)
                .playerTwoScore(0)
                .status(GameSessionStatus.ACTIVE)
                .build();

        GameSession savedSession = gameSessionRepository.save(session);
        saveRoundScore(savedSession);

        GameSessionResponseDto response = toResponse(savedSession);
        eventPublisher.publishEvent(new SessionStartedEvent(savedSession.getId(), response));

        return response;
    }

    public List<GameSessionResponseDto> getSessions(User user) {
        return gameSessionRepository.findByUserOrderByStartedAtDesc(user)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public PageResponseDto<GameSessionResponseDto> getMatchHistory(
            User user,
            int page,
            int size,
            String search,
            String faction,
            String result
    ) {
        int safePage = Math.max(page, 0);
        int safeSize = Math.min(Math.max(size, 1), 50);
        Pageable pageable = PageRequest.of(safePage, safeSize);

        Page<GameSession> sessionPage = gameSessionRepository.searchMatchHistory(
                user,
                GameSessionStatus.FINISHED,
                trimOrEmpty(search),
                trimOrEmpty(faction),
                normalizeResult(result),
                pageable
        );

        List<GameSessionResponseDto> content = sessionPage.getContent()
                .stream()
                .map(this::toResponse)
                .toList();

        return new PageResponseDto<>(
                content,
                sessionPage.getNumber(),
                sessionPage.getSize(),
                sessionPage.getTotalElements(),
                sessionPage.getTotalPages(),
                sessionPage.isFirst(),
                sessionPage.isLast()
        );
    }

    public GameSessionResponseDto getActiveSession(User user) {
        return gameSessionRepository.findFirstByUserAndStatusOrderByStartedAtDesc(
                        user,
                        GameSessionStatus.ACTIVE
                )
                .map(this::toResponse)
                .orElse(null);
    }

    public GameSessionResponseDto getSession(User user, Long sessionId) {
        GameSession session = findSessionForUser(user, sessionId);
        return toResponse(session);
    }

    @Transactional
    public GameSessionResponseDto updateScore(
            User user,
            Long sessionId,
            GameSessionScoreRequestDto request
    ) {
        GameSession session = findActiveSessionForUser(user, sessionId);

        session.setPlayerOneScore(request.getPlayerOneScore());
        session.setPlayerTwoScore(request.getPlayerTwoScore());

        GameSession savedSession = gameSessionRepository.save(session);
        saveRoundScore(savedSession);

        GameSessionResponseDto response = toResponse(savedSession);
        eventPublisher.publishEvent(new SessionUpdatedEvent(
                savedSession.getId(),
                "SCORE_UPDATED",
                response
        ));

        return response;
    }

    @Transactional
    public GameSessionResponseDto updateRound(
            User user,
            Long sessionId,
            GameSessionRoundRequestDto request
    ) {
        GameSession session = findActiveSessionForUser(user, sessionId);

        saveRoundScore(session);
        session.setCurrentRound(request.getCurrentRound());

        GameSession savedSession = gameSessionRepository.save(session);
        saveRoundScore(savedSession);

        GameSessionResponseDto response = toResponse(savedSession);
        eventPublisher.publishEvent(new SessionUpdatedEvent(
                savedSession.getId(),
                "ROUND_UPDATED",
                response
        ));

        return response;
    }

    @Transactional
    public GameSessionResponseDto endSession(User user, Long sessionId) {
        GameSession session = findActiveSessionForUser(user, sessionId);

        saveRoundScore(session);
        session.setStatus(GameSessionStatus.FINISHED);
        session.setEndedAt(LocalDateTime.now());

        GameSession savedSession = gameSessionRepository.save(session);
        GameSessionResponseDto response = toResponse(savedSession);

        eventPublisher.publishEvent(new SessionUpdatedEvent(
                savedSession.getId(),
                "SESSION_ENDED",
                response
        ));

        return response;
    }

    @Transactional
    public GameSessionResponseDto updateSavedMatch(
            User user,
            Long sessionId,
            GameSessionUpdateRequestDto request
    ) {
        GameSession session = findFinishedSessionForUser(user, sessionId);

        session.setPlayerOneName(trimOrNull(request.getPlayerOneName()));
        session.setPlayerTwoName(trimOrNull(request.getPlayerTwoName()));
        session.setPlayerOneFaction(trimOrNull(request.getPlayerOneFaction()));
        session.setPlayerTwoFaction(trimOrNull(request.getPlayerTwoFaction()));
        session.setMissionName(trimOrNull(request.getMissionName()));
        session.setDeploymentMap(trimOrNull(request.getDeploymentMap()));
        session.setNotes(trimOrNull(request.getNotes()));
        session.setPlayerOneScore(request.getPlayerOneScore());
        session.setPlayerTwoScore(request.getPlayerTwoScore());

        GameSession savedSession = gameSessionRepository.save(session);
        saveRoundScore(savedSession);

        return toResponse(savedSession);
    }

    @Transactional
    public void deleteSavedMatch(User user, Long sessionId) {
        GameSession session = findFinishedSessionForUser(user, sessionId);

        diceRollRepository.deleteByGameSession(session);
        roundScoreRepository.deleteByGameSession(session);
        gameSessionRepository.delete(session);
    }

    private GameSessionRoundScore saveRoundScore(GameSession session) {
        GameSessionRoundScore roundScore = roundScoreRepository
                .findByGameSessionAndRoundNumber(session, session.getCurrentRound())
                .orElseGet(() -> GameSessionRoundScore.builder()
                        .gameSession(session)
                        .roundNumber(session.getCurrentRound())
                        .build());

        roundScore.setPlayerOneScore(session.getPlayerOneScore());
        roundScore.setPlayerTwoScore(session.getPlayerTwoScore());

        return roundScoreRepository.save(roundScore);
    }

    private GameSessionResponseDto toResponse(GameSession session) {
        List<GameSessionRoundScore> roundScores = roundScoreRepository
                .findByGameSessionOrderByRoundNumberAsc(session);

        return GameSessionResponseDto.fromSession(session, roundScores);
    }

    private GameSession findSessionForUser(User user, Long sessionId) {
        return gameSessionRepository.findByIdAndUser(sessionId, user)
                .orElseThrow(() -> new IllegalArgumentException("Session was not found"));
    }

    private GameSession findActiveSessionForUser(User user, Long sessionId) {
        GameSession session = findSessionForUser(user, sessionId);

        if (session.getStatus() != GameSessionStatus.ACTIVE) {
            throw new IllegalArgumentException("This session is already finished");
        }

        return session;
    }

    private GameSession findFinishedSessionForUser(User user, Long sessionId) {
        GameSession session = findSessionForUser(user, sessionId);

        if (session.getStatus() != GameSessionStatus.FINISHED) {
            throw new IllegalArgumentException("Only saved matches can be changed");
        }

        return session;
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

    private String normalizeResult(String result) {
        String trimmedResult = trimOrEmpty(result).toUpperCase();

        if (trimmedResult.equals("VICTORY")
                || trimmedResult.equals("DEFEAT")
                || trimmedResult.equals("DRAW")) {
            return trimmedResult;
        }

        return "";
    }
}