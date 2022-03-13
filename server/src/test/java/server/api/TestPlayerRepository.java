package server.api;

import commons.Player;
import server.database.PlayerRepository;

public class TestPlayerRepository extends StubRepository<Player, Long> implements PlayerRepository {
    public TestPlayerRepository() {
        super(Player.class);
    }
}
