package backend.data.repository;

import backend.domain.ArmyList;
import backend.domain.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ArmyListRepository extends JpaRepository<ArmyList, Long> {
    Optional<ArmyList> findByIdAndUser(Long id, User user);

    @Query("""
            SELECT armyList
            FROM ArmyList armyList
            JOIN armyList.gameEdition edition
            WHERE armyList.user = :user
              AND (
                    :search IS NULL OR :search = '' OR
                    LOWER(armyList.name) LIKE LOWER(CONCAT('%', :search, '%')) OR
                    LOWER(armyList.faction) LIKE LOWER(CONCAT('%', :search, '%')) OR
                    LOWER(COALESCE(armyList.description, '')) LIKE LOWER(CONCAT('%', :search, '%'))
              )
              AND (
                    :faction IS NULL OR :faction = '' OR
                    LOWER(armyList.faction) = LOWER(:faction)
              )
              AND (
                    :editionCode IS NULL OR :editionCode = '' OR
                    UPPER(edition.code) = UPPER(:editionCode)
              )
            ORDER BY armyList.updatedAt DESC, armyList.createdAt DESC
            """)
    Page<ArmyList> searchArmyLists(
            @Param("user") User user,
            @Param("search") String search,
            @Param("faction") String faction,
            @Param("editionCode") String editionCode,
            Pageable pageable
    );
}