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
        var savedPlayer1 = lbc.addPlayerForcibly(new Player("david", 10)).getBody();
        int player1BestSingleScore = savedPlayer1.getBestSingleScore();
        assertEquals(10, player1BestSingleScore);

        Player testPlayer = new Player("testPlayer", 20);
        lbc.addPlayerForcibly(testPlayer);
        int testPlayerBestSingleScore = testPlayer.bestSingleScore;
        assertEquals(20, testPlayerBestSingleScore);
    }

    @Test
    void getPlayerMultiScore() {
        var savedPlayer1 = lbc.addPlayerForcibly(new Player("david", 10)).getBody();
        int player1BestMultiScore = savedPlayer1.getBestMultiScore();
        assertEquals(10, player1BestMultiScore);

        Player testPlayer = new Player("testPlayer", 20);
        lbc.addPlayerForcibly(testPlayer);
        int testPlayerBestMultiScore = testPlayer.bestMultiScore;
        assertEquals(20, testPlayerBestMultiScore);
    }

    @Test
    void updateBestSingleScore() {
        var savedPlayer1 = lbc.addPlayerForcibly(new Player("david", 10)).getBody();
        assertEquals(savedPlayer1.bestSingleScore, 10);
        System.out.println(savedPlayer1.toString());
        System.out.println(lbc.getPlayerById(1L).toString());
        savedPlayer1.setBestSingleScore(20);
        System.out.println(lbc.getPlayerById(1L).getBody().getBestSingleScore());
        assertEquals(20, savedPlayer1.bestSingleScore);

    }

    @Test
    void updateBestMultiScore() {
        var savedPlayer1 = lbc.addPlayerForcibly(new Player("david", 10)).getBody();
        assertEquals(savedPlayer1.bestMultiScore, 10);
        System.out.println(savedPlayer1.toString());
        System.out.println(lbc.getPlayerById(1L).toString());
        savedPlayer1.setBestMultiScore(20);
        System.out.println(lbc.getPlayerById(1L).getBody().getBestMultiScore());
        assertEquals(20,savedPlayer1.bestMultiScore );
    }

    @Test
    void updateCurrentSingleScore() {
        var savedPlayer1 = lbc.addPlayerForcibly(new Player("david", 10)).getBody();
        lbc.updateCurrentSinglePoints(1L, 1110);
        assertEquals(1110, savedPlayer1.getCurrentPoints());
    }

    @Test
    void updateCurrentMultiScore() {
        var savedPlayer1 = lbc.addPlayerForcibly(new Player("david", 10)).getBody();
        lbc.updateCurrentMultiPoints(1L, 1110);
        assertEquals(1110, savedPlayer1.getCurrentPoints());
    }

}
