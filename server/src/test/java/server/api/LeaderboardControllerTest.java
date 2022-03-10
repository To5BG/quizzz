package server.api;

import commons.Player;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

class LeaderboardControllerTest {

    private Random random;
    private TestPlayerRepository testRepo;

    private LeaderboardController lbc;

    @BeforeEach
    public void setup() {
        random = new Random();
        testRepo = new TestPlayerRepository();
        lbc = new LeaderboardController(testRepo);
    }

    @Test
    public void constructorTest() {
        LeaderboardController temp = new LeaderboardController(testRepo);
        assertNotNull(temp);
    }

    @Test
    void getAllPlayers() {
        var players = lbc.getAllPlayers().getBody();
        assertTrue(players.size() == 0);

        lbc.addPlayerForcibly(new Player("David", 10));
        assertTrue(lbc.getAllPlayers().getBody().size() == 1);

        lbc.addPlayerForcibly(new Player("Yongcheng", 15));
        assertTrue(lbc.getAllPlayers().getBody().size() == 2);
    }

    @Test
    void getPlayerById() {
        Optional<Player> temp = Optional.ofNullable(lbc.addPlayerForcibly(
                new Player("david", 10)).getBody());
        lbc.addPlayerForcibly(new Player("david", 10));
        var player = testRepo.findById(1L);
        assertEquals(player, temp);
    }

    @Test
    void addPlayerToTheRepository() {
        var savedPlayer = lbc.addPlayerForcibly(new Player("david", 10)).getBody();
        assertTrue(testRepo.calledMethods.contains("save"));
        assertEquals(savedPlayer, testRepo.findAll().get(0));
    }
}