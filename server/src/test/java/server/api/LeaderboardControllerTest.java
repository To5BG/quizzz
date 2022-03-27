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
    void getPlayerById() {
        Player newPlayer = new Player("david", 10);
        Optional<Player> temp = Optional.ofNullable(lbc.addPlayerForcibly(
                newPlayer).getBody());
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

    @Test
    void getPlayerSingleScores() {
        var savedPlayer1 = lbc.addPlayerForcibly(new Player("test1", 10)).getBody();
        var savedPlayer2 = lbc.addPlayerForcibly(new Player("test2", 20)).getBody();
        var savedPlayer3 = lbc.addPlayerForcibly(new Player("test3", 30)).getBody();
        var list = lbc.getPlayerSingleScores();
        var temp = list.getBody();
        System.out.println(temp.size());
        assertEquals(3, temp.size());
        System.out.println(temp.toString());
        assertEquals(savedPlayer3, temp.get(0));
        assertEquals(savedPlayer2, temp.get(1));
        assertEquals(savedPlayer1, temp.get(2));
    }

    @Test
    void getPlayerMultiScore() {
        var savedPlayer1 = lbc.addPlayerForcibly(new Player("test1", 10)).getBody();
        var savedPlayer2 = lbc.addPlayerForcibly(new Player("test2", 20)).getBody();
        var savedPlayer3 = lbc.addPlayerForcibly(new Player("test3", 30)).getBody();
        var list = lbc.getPlayerMultiScore();
        var temp = list.getBody();
        System.out.println(temp.size());
        assertEquals(3, temp.size());
        System.out.println(temp.toString());
        assertEquals(savedPlayer3, temp.get(0));
        assertEquals(savedPlayer2, temp.get(1));
        assertEquals(savedPlayer1, temp.get(2));
    }

    @Test
    void updateBestSingleScore() {

        var savedPlayer1 = lbc.addPlayerForcibly(new Player("david", 10)).getBody();
        assertEquals(10, savedPlayer1.getBestSingleScore());
        lbc.updateBestSingleScore(1L, 200);
        assertEquals(200, savedPlayer1.getBestSingleScore());

    }

    @Test
    void updateBestMultiScore() {

        var savedPlayer1 = lbc.addPlayerForcibly(new Player("david", 10)).getBody();
        assertEquals(10, savedPlayer1.getBestMultiScore());
        lbc.updateBestMultiScore(1L, 2000);
        assertEquals(2000, savedPlayer1.bestMultiScore);

    }

    @Test
    void updateCurrentSingleScore() {
        var savedPlayer1 = lbc.addPlayerForcibly(new Player("david", 10)).getBody();
        assertEquals(0, savedPlayer1.getCurrentPoints());
        lbc.updateCurrentSinglePoints(1L, 1110);
        assertEquals(1110, savedPlayer1.getCurrentPoints());
    }

    @Test
    void updateCurrentMultiScore() {
        var savedPlayer1 = lbc.addPlayerForcibly(new Player("david", 10)).getBody();
        assertEquals(0, savedPlayer1.getCurrentPoints());
        lbc.updateCurrentMultiPoints(1L, 1110);
        assertEquals(1110, savedPlayer1.getCurrentPoints());
    }

}
