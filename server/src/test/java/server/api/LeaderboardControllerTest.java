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

    /**
     * The setup for all test
     */
    @BeforeEach
    public void setup() {
        random = new Random();
        testRepo = new TestPlayerRepository();
        lbc = new LeaderboardController(testRepo);
    }

    /**
     * The test of constructor in LeaderboardController
     */
    @Test
    public void constructorTest() {
        //Test the constructor
        LeaderboardController temp = new LeaderboardController(testRepo);
        assertNotNull(temp);

        //Test the constructor after being modified
        LeaderboardController test = new LeaderboardController(testRepo);
        testRepo.findByOrderByBestMultiScoreDesc();
        assertNotNull(test);
    }

    /**
     * The test of method getPlayerById
     */
    @Test
    void getPlayerById() {
        //Test the Get player by id method
        Player newPlayer = new Player("david", 10);
        Optional<Player> temp = Optional.ofNullable(lbc.addPlayerForcibly(
                newPlayer).getBody());
        lbc.addPlayerForcibly(new Player("david", 10));

        //Test successful get
        var player = lbc.getPlayerById(1L);
        assertEquals(temp.get(), player.getBody());

        //Test BedRequest
        ResponseEntity<Player> wrong = lbc.getPlayerById(42L);
        assertEquals(HttpStatus.BAD_REQUEST, wrong.getStatusCode());
    }

    /**
     * The test of addPlayerForcibly method
     */
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

    /**
     * The test of getPlayerSingleScore method
     */
    @Test
    void getPlayerSingleScores() {
        //Test get method
        var savedPlayer1 = lbc.addPlayerForcibly(new Player("david", 10)).getBody();
        int Player1BestSingleScore = savedPlayer1.getBestSingleScore();
        assertEquals(10, Player1BestSingleScore);

        //Test the new variable created
        Player testPlayer = new Player("testPlayer", 20);
        lbc.addPlayerForcibly(testPlayer);
        int testPlayerBestSingleScore = testPlayer.bestSingleScore;
        assertEquals(20, testPlayerBestSingleScore);
    }

    @Test
    void getPlayerMultiScore() {
        //Test get method
        var savedPlayer1 = lbc.addPlayerForcibly(new Player("david", 10)).getBody();
        int Player1BestMultiScore = savedPlayer1.getBestMultiScore();
        assertEquals(10, Player1BestMultiScore);

        //Test the new variable created
        Player testPlayer = new Player("testPlayer", 20);
        lbc.addPlayerForcibly(testPlayer);
        int testPlayerBestMultiScore = testPlayer.bestMultiScore;
        assertEquals(20, testPlayerBestMultiScore);
    }

    @Test
    void updateBestSingleScore() {
        //Test updateCurrentSinglePoints
        var savedPlayer1 = lbc.addPlayerForcibly(new Player("david", 10)).getBody();
        assertEquals(savedPlayer1.bestSingleScore, 10);
        System.out.println(savedPlayer1.toString());
        System.out.println(lbc.getPlayerById(1L).toString());
        savedPlayer1.setBestSingleScore(20);
        System.out.println(lbc.getPlayerById(1L).getBody().getBestSingleScore());
        assertEquals(savedPlayer1.bestSingleScore, 20);

    }

    @Test
    void updateBestMultiScore() {
        //Test updateCurrentMultiPoints
        var savedPlayer1 = lbc.addPlayerForcibly(new Player("david", 10)).getBody();
        assertEquals(savedPlayer1.bestMultiScore, 10);
        System.out.println(savedPlayer1.toString());
        System.out.println(lbc.getPlayerById(1L).toString());
        savedPlayer1.setBestMultiScore(20);
        System.out.println(lbc.getPlayerById(1L).getBody().getBestMultiScore());
        assertEquals(savedPlayer1.bestMultiScore, 20);
    }

    @Test
    void updateCurrentSingleScore() {
        //Test updateCurrentSingleScore
        var savedPlayer1 = lbc.addPlayerForcibly(new Player("david", 10)).getBody();
        assertEquals(savedPlayer1.getCurrentPoints(), 0);
        savedPlayer1.setCurrentPoints(10);
        assertEquals(savedPlayer1.currentPoints, 10);
    }

    @Test
    void updateCurrentMultiScore() {
        //Test updateCurrentSingleScore
        var savedPlayer1 = lbc.addPlayerForcibly(new Player("david", 10)).getBody();
        assertEquals(savedPlayer1.getCurrentPoints(), 0);
        savedPlayer1.setCurrentPoints(10);
        assertEquals(savedPlayer1.currentPoints, 10);
    }
}
