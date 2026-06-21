package backend.data.repository;

import backend.domain.DiceRoll;
import backend.domain.GameSession;
import backend.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DiceRollRepository extends JpaRepository<DiceRoll, Long> {
    List<DiceRoll> findTop20ByUserOrderByCreatedAtDesc(User user);

    Optional<DiceRoll> findFirstByUserOrderByCreatedAtDesc(User user);

    Optional<DiceRoll> findByIdAndUser(Long id, User user);

    List<DiceRoll> findTop20ByUserAndGameSessionOrderByCreatedAtDesc(
            User user,
            GameSession gameSession
    );

    Optional<DiceRoll> findFirstByUserAndGameSessionOrderByCreatedAtDesc(
            User user,
            GameSession gameSession
    );

    void deleteByGameSession(GameSession gameSession);
}