package server.database;

import commons.Player;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PlayerRepository extends JpaRepository<Player, Long> {
    /**
     * the JPA sorting method to return a list of players sorted by their best single score
     *
     * @return a list of ordered players
     */
    List<Player> findByOrderByBestSingleScoreDesc();
    //since we don't have a separate hibernateUtil here, I suggest use the local way to do this
    //with a simpler way

    /**
     * the JPA sorting method to return a list of players sorted by their best multimode score
     *
     * @return a list of ordered players
     */
    List<Player> findByOrderByBestMultiScoreDesc();

    /**
     * the JPA sorting method to return a list of players sorted by their best time attack score
     *
     * @return a list of ordered players
     */
    List<Player> findByOrderByBestTimeAttackScoreDesc();

    /**
     * the JPA sorting method to return a list of player sorted by their best survival score
     *
     * @return a list of ordered players
     */
    List<Player> findByOrderByBestSurvivalScoreDesc();

}
