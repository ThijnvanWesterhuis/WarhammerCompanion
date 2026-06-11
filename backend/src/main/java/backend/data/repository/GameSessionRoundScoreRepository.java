package backend.data.repository;

import backend.domain.GameSession;
import backend.domain.GameSessionRoundScore;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface GameSessionRoundScoreRepository extends JpaRepository<GameSessionRoundScore, Long> {
    List<GameSessionRoundScore> findByGameSessionOrderByRoundNumberAsc(GameSession gameSession);

    Optional<GameSessionRoundScore> findByGameSessionAndRoundNumber(
            GameSession gameSession,
            Integer roundNumber
    );

    void deleteByGameSession(GameSession gameSession);
}