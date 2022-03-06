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
        lbc = new LeaderboardController(random, testRepo);
    }

    @Test
    public void constructorTest() {
        LeaderboardController temp = new LeaderboardController(random, testRepo);
        assertNotNull(temp);
    }

    @Test
    void getAllPlayers() {
        var players = lbc.getAllPlayers();
        System.out.println(players.size());
        assertTrue(players.size() == 0);

        lbc.addPlayerToRepository(new Player("David", 10));
        System.out.println(players.size());
        assertTrue(players.size() == 1);

        lbc.addPlayerToRepository(new Player("Yongcheng", 15));
        System.out.println(players.size());
        assertTrue(players.size() == 2);
    }

    @Test
    void getPlayerById() {
        Optional<Player> temp = Optional.ofNullable(lbc.addPlayerToRepository(new Player("david", 10)).getBody());
        lbc.addPlayerToRepository(new Player("david", 10));
        var player = testRepo.findById(1L);
        assertEquals(player, temp);
    }

    @Test
    void addPlayerToTheRepository() {
        var savedPlayer = lbc.addPlayerToRepository(new Player("david", 10)).getBody();
        assertTrue(testRepo.calledMethods.contains("save"));
        assertEquals(savedPlayer, testRepo.findAll().get(0));
    }
}