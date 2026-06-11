package backend.api.dto;

import backend.domain.GameSession;
import backend.domain.GameSessionRoundScore;
import backend.domain.GameSessionStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@AllArgsConstructor
public class GameSessionResponseDto {
    private Long id;
    private String playerOneName;
    private String playerTwoName;
    private String playerOneFaction;
    private String playerTwoFaction;
    private String missionName;
    private String deploymentMap;
    private String notes;
    private Integer currentRound;
    private Integer playerOneScore;
    private Integer playerTwoScore;
    private GameSessionStatus status;
    private String result;
    private Long elapsedSeconds;
    private LocalDateTime startedAt;
    private LocalDateTime endedAt;
    private List<GameSessionRoundScoreResponseDto> roundScores;

    public static GameSessionResponseDto fromSession(GameSession session) {
        return fromSession(session, List.of());
    }

    public static GameSessionResponseDto fromSession(
            GameSession session,
            List<GameSessionRoundScore> roundScores
    ) {
        return new GameSessionResponseDto(
                session.getId(),
                session.getPlayerOneName(),
                session.getPlayerTwoName(),
                session.getPlayerOneFaction(),
                session.getPlayerTwoFaction(),
                session.getMissionName(),
                session.getDeploymentMap(),
                session.getNotes(),
                session.getCurrentRound(),
                session.getPlayerOneScore(),
                session.getPlayerTwoScore(),
                session.getStatus(),
                calculateResult(session),
                calculateElapsedSeconds(session),
                session.getStartedAt(),
                session.getEndedAt(),
                roundScores.stream()
                        .map(GameSessionRoundScoreResponseDto::fromRoundScore)
                        .toList()
        );
    }

    private static Long calculateElapsedSeconds(GameSession session) {
        if (session.getStartedAt() == null) {
            return 0L;
        }

        LocalDateTime endTime = session.getEndedAt() == null
                ? LocalDateTime.now()
                : session.getEndedAt();

        return Duration.between(session.getStartedAt(), endTime).getSeconds();
    }

    private static String calculateResult(GameSession session) {
        if (session.getStatus() == GameSessionStatus.ACTIVE) {
            return "IN_PROGRESS";
        }

        if (session.getPlayerOneScore() > session.getPlayerTwoScore()) {
            return "VICTORY";
        }

        if (session.getPlayerOneScore() < session.getPlayerTwoScore()) {
            return "DEFEAT";
        }

        return "DRAW";
    }
}