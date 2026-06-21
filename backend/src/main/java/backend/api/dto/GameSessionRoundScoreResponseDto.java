package backend.api.dto;

import backend.domain.GameSessionRoundScore;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class GameSessionRoundScoreResponseDto {
    private Long id;
    private Integer roundNumber;
    private Integer playerOneScore;
    private Integer playerTwoScore;
    private LocalDateTime updatedAt;

    public static GameSessionRoundScoreResponseDto fromRoundScore(GameSessionRoundScore roundScore) {
        return new GameSessionRoundScoreResponseDto(
                roundScore.getId(),
                roundScore.getRoundNumber(),
                roundScore.getPlayerOneScore(),
                roundScore.getPlayerTwoScore(),
                roundScore.getUpdatedAt()
        );
    }
}