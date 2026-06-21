package backend.data.repository;

import backend.domain.GameEdition;
import backend.domain.UnitCatalog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UnitCatalogRepository extends JpaRepository<UnitCatalog, Long> {
    List<UnitCatalog> findByGameEditionAndFactionIgnoreCaseOrderByNameAsc(
            GameEdition gameEdition,
            String faction
    );

    boolean existsByGameEditionAndFactionIgnoreCaseAndNameIgnoreCase(
            GameEdition gameEdition,
            String faction,
            String name
    );
}