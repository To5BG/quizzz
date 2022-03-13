package server.api;

import commons.Player;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

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
    void getPlayerSingleScoreTest() {
        var players = lbc.getPlayerSingleScores().getBody();
        assertTrue(players.size() == 0);

        lbc.addPlayerForcibly(new Player("David", 10));
        assertTrue(lbc.getPlayerSingleScores().getBody().size() == 1);

        lbc.addPlayerForcibly(new Player("Yongcheng", 15));
        assertTrue(lbc.getPlayerSingleScores().getBody().size() == 2);
    }

    @Test
    void getPlayerById() {
        Optional<Player> temp = Optional.ofNullable(lbc.addPlayerForcibly(
                new Player("david", 10)).getBody());
        lbc.addPlayerForcibly(new Player("david", 10));

        var player = lbc.getPlayerById(1L);
        assertEquals(temp.get(), player.getBody());

        ResponseEntity<Player> wrong = lbc.getPlayerById(42L);
        assertEquals(HttpStatus.BAD_REQUEST, wrong.getStatusCode());
    }

    @Test
    void addPlayerForcibly() {
        var savedPlayer = lbc.addPlayerForcibly(new Player("david", 10)).getBody();
        assertTrue(testRepo.calledMethods.contains("save"));
        assertEquals(savedPlayer, testRepo.findAll().get(0));

        ResponseEntity<Player> resp = lbc.addPlayerForcibly(null);
        assertEquals(HttpStatus.BAD_REQUEST, resp.getStatusCode());

        resp = lbc.addPlayerForcibly(new Player(null, 0));
        assertEquals(HttpStatus.BAD_REQUEST, resp.getStatusCode());

        resp = lbc.addPlayerForcibly(new Player("", 0));
        assertEquals(HttpStatus.BAD_REQUEST, resp.getStatusCode());
    }
}