package backend.data.repository;

import backend.domain.DicePreset;
import backend.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DicePresetRepository extends JpaRepository<DicePreset, Long> {
    List<DicePreset> findByUserOrderByNameAsc(User user);

    Optional<DicePreset> findByIdAndUser(Long id, User user);

    boolean existsByUserAndNameIgnoreCase(User user, String name);
}