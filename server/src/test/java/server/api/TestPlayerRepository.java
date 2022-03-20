package server.api;

import commons.Player;
import server.database.PlayerRepository;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class TestPlayerRepository extends StubRepository<Player, Long> implements PlayerRepository {
    public TestPlayerRepository() {
        super(Player.class);
    }

    /**
     * the sorting in the TestPlayerRepository for Best Single Score
     * @return a list of players sorted by the best Single Score
     */
    @Override
    public List<Player> findByOrderByBestSingleScoreDesc() {
       var returnList =  findAll().stream().sorted(Comparator
                       .comparing(Player::getBestSingleScore).reversed()).collect(Collectors.toList());
       return returnList;
    }

    /**
     * the sorting in the TestPlayerRepository for Best Multi Score
     * @return a list of players sorted by the best Multi Score
     */
    @Override
    public List<Player> findByOrderByBestMultiScoreDesc() {
        var returnList =  findAll().stream().sorted(Comparator
                .comparing(Player::getBestMultiScore).reversed()).collect(Collectors.toList());
        return returnList;
    }

}
