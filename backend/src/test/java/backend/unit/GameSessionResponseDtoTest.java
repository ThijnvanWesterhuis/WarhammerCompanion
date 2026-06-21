package backend.unit;

import backend.api.dto.GameSessionResponseDto;
import backend.domain.GameSession;
import backend.domain.GameSessionRoundScore;
import backend.domain.GameSessionStatus;
import backend.domain.Role;
import backend.domain.User;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class GameSessionResponseDtoTest {

    @Test
    void activeSessionResultIsInProgress() {
        GameSession session = sessionWithScores(5, 3, GameSessionStatus.ACTIVE);

        GameSessionResponseDto response = GameSessionResponseDto.fromSession(session);

        assertEquals("IN_PROGRESS", response.getResult());
    }

    @Test
    void finishedSessionResultIsVictoryWhenPlayerOneHasHigherScore() {
        GameSession session = sessionWithScores(72, 61, GameSessionStatus.FINISHED);

        GameSessionResponseDto response = GameSessionResponseDto.fromSession(session);

        assertEquals("VICTORY", response.getResult());
    }

    @Test
    void finishedSessionResultIsDefeatWhenPlayerOneHasLowerScore() {
        GameSession session = sessionWithScores(40, 55, GameSessionStatus.FINISHED);

        GameSessionResponseDto response = GameSessionResponseDto.fromSession(session);

        assertEquals("DEFEAT", response.getResult());
    }

    @Test
    void finishedSessionResultIsDrawWhenScoresAreEqual() {
        GameSession session = sessionWithScores(45, 45, GameSessionStatus.FINISHED);

        GameSessionResponseDto response = GameSessionResponseDto.fromSession(session);

        assertEquals("DRAW", response.getResult());
    }

    @Test
    void fromSessionMapsRoundScores() {
        GameSession session = sessionWithScores(10, 8, GameSessionStatus.ACTIVE);
        GameSessionRoundScore roundScore = GameSessionRoundScore.builder()
                .id(20L)
                .gameSession(session)
                .roundNumber(1)
                .playerOneScore(10)
                .playerTwoScore(8)
                .updatedAt(LocalDateTime.now())
                .build();

        GameSessionResponseDto response = GameSessionResponseDto.fromSession(session, List.of(roundScore));

        assertEquals(1, response.getRoundScores().size());
        assertEquals(1, response.getRoundScores().get(0).getRoundNumber());
        assertEquals(10, response.getRoundScores().get(0).getPlayerOneScore());
    }

    private GameSession sessionWithScores(int playerOneScore, int playerTwoScore, GameSessionStatus status) {
        LocalDateTime startedAt = LocalDateTime.now().minusMinutes(5);
        LocalDateTime endedAt = status == GameSessionStatus.FINISHED ? LocalDateTime.now() : null;

        return GameSession.builder()
                .id(1L)
                .user(testUser())
                .playerOneName("Thijn")
                .playerTwoName("Opponent")
                .playerOneScore(playerOneScore)
                .playerTwoScore(playerTwoScore)
                .currentRound(1)
                .status(status)
                .startedAt(startedAt)
                .endedAt(endedAt)
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