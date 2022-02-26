package server.database;

import commons.GameSession;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SessionRepository extends JpaRepository<GameSession, Long> {
}
