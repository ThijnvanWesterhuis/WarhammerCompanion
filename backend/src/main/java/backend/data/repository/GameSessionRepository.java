package backend.data.repository;

import backend.domain.GameSession;
import backend.domain.GameSessionStatus;
import backend.domain.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface GameSessionRepository extends JpaRepository<GameSession, Long> {
    List<GameSession> findByUserOrderByStartedAtDesc(User user);

    Optional<GameSession> findByIdAndUser(Long id, User user);

    Optional<GameSession> findFirstByUserAndStatusOrderByStartedAtDesc(
            User user,
            GameSessionStatus status
    );

    @Query("""
            SELECT session
            FROM GameSession session
            WHERE session.user = :user
              AND session.status = :status
              AND (
                    :search IS NULL OR :search = '' OR
                    LOWER(COALESCE(session.playerOneName, '')) LIKE LOWER(CONCAT('%', :search, '%')) OR
                    LOWER(COALESCE(session.playerTwoName, '')) LIKE LOWER(CONCAT('%', :search, '%')) OR
                    LOWER(COALESCE(session.playerOneFaction, '')) LIKE LOWER(CONCAT('%', :search, '%')) OR
                    LOWER(COALESCE(session.playerTwoFaction, '')) LIKE LOWER(CONCAT('%', :search, '%')) OR
                    LOWER(COALESCE(session.missionName, '')) LIKE LOWER(CONCAT('%', :search, '%')) OR
                    LOWER(COALESCE(session.deploymentMap, '')) LIKE LOWER(CONCAT('%', :search, '%')) OR
                    LOWER(COALESCE(session.notes, '')) LIKE LOWER(CONCAT('%', :search, '%'))
              )
              AND (
                    :faction IS NULL OR :faction = '' OR
                    LOWER(COALESCE(session.playerOneFaction, '')) = LOWER(:faction) OR
                    LOWER(COALESCE(session.playerTwoFaction, '')) = LOWER(:faction)
              )
              AND (
                    :result IS NULL OR :result = '' OR
                    (:result = 'VICTORY' AND session.playerOneScore > session.playerTwoScore) OR
                    (:result = 'DEFEAT' AND session.playerOneScore < session.playerTwoScore) OR
                    (:result = 'DRAW' AND session.playerOneScore = session.playerTwoScore)
              )
            ORDER BY session.endedAt DESC, session.startedAt DESC
            """)
    Page<GameSession> searchMatchHistory(
            @Param("user") User user,
            @Param("status") GameSessionStatus status,
            @Param("search") String search,
            @Param("faction") String faction,
            @Param("result") String result,
            Pageable pageable
    );
}