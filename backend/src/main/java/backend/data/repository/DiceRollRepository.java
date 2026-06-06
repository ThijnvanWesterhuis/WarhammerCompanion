package backend.data.repository;

import backend.domain.DiceRoll;
import backend.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DiceRollRepository extends JpaRepository<DiceRoll, Long> {
    List<DiceRoll> findTop20ByUserOrderByCreatedAtDesc(User user);
}