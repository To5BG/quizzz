package server.api;

import commons.Player;
import server.database.PlayerRepository;

import java.util.List;

public class TestPlayerRepository extends StubRepository<Player, Long> implements PlayerRepository {
    public TestPlayerRepository() {
        super(Player.class);
    }

    @Override
    public List<Player> findByOrderByBestSingleScoreDesc() {
        return null;
    }

    @Override
    public List<Player> findByOrderByBestMultiScoreDesc() {
        return null;
    }

}
