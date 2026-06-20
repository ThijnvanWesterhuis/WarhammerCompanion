package backend.data.repository;

import backend.domain.GameEdition;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface GameEditionRepository extends JpaRepository<GameEdition, Long> {
    Optional<GameEdition> findByCode(String code);

    boolean existsByCode(String code);

    List<GameEdition> findAllByOrderByReleaseOrderDesc();
}