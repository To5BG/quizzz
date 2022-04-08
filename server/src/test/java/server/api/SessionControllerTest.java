/*
 * Copyright 2021 Delft University of Technology
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package server.api;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.OK;

import java.util.List;
import java.util.Random;

import commons.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

public class SessionControllerTest {

    public int nextInt;
    private MyRandom random;
    private TestPlayerRepository playerRepo;
    private TestActivityRepository activityRepo;
    private StubSessionManager stubSessionManager;
    private LeaderboardController lbc;

    private SessionController sut;
    private GameSession first;
    private GameSession waiting;
    private GameSession singleplayer;

    @BeforeEach
    public void setup() {
        random = new MyRandom();
        playerRepo = new TestPlayerRepository();
        activityRepo = new TestActivityRepository();
        activityRepo.save(new Activity("test", 42L, "test", "test"));
        activityRepo.save(new Activity("test2", 43L, "test2", "test2"));
        activityRepo.save(new Activity("test3", 44L, "test3", "test3"));
        activityRepo.save(new Activity("test4", 45L, "test4", "test4"));

        stubSessionManager = new StubSessionManager();
        lbc = new LeaderboardController(playerRepo);
        sut = new SessionController(random, playerRepo, "test", stubSessionManager,
                new ActivityController(new Random(), activityRepo), lbc);
        first = new GameSession(GameSession.SessionType.MULTIPLAYER);
        waiting = new GameSession(GameSession.SessionType.WAITING_AREA);
        singleplayer = new GameSession(GameSession.SessionType.SINGLEPLAYER);
    }

    @Test
    public void testIsUsernameActive() {
        sut.addSession(waiting);
        assertFalse(sut.isUsernameActive("beniGhost"));
        Player p = new Player("beniGhost", 1337);
        sut.addPlayer(waiting.id, p);
        assertTrue(sut.isUsernameActive("beniGhost"));
    }

    @Test
    public void testSetGameRounds() {
        first = sut.addSession(first).getBody();
        sut.setGameRounds(first.id, 1337);
        assertEquals(1337, sut.getSessionById(first.id).getBody().gameRounds);
    }

    @Test
    public void testSetGameRoundsNoSession() {
        var resp = sut.setGameRounds(42L, 1337);
        assertEquals(BAD_REQUEST, resp.getStatusCode());
    }

    @Test
    public void testSetQuestionCounter() {
        first = sut.addSession(first).getBody();
        sut.setQuestionCounter(first.id, 1337);
        assertEquals(1337, sut.getSessionById(first.id).getBody().questionCounter);
    }

    @Test
    public void testSetQuestionCounterNoSession() {
        var resp = sut.setQuestionCounter(42L, 1337);
        assertEquals(BAD_REQUEST, resp.getStatusCode());
    }

    @Test
    public void testDisableLeaderboard() {
        first = sut.addSession(first).getBody();
        sut.disableLeaderboard(first.id);
        assertTrue(sut.getSessionById(first.id).getBody().isLeaderboardDisabled);
    }

    @Test
    public void testDisableLeaderboardNoSession() {
        var resp = sut.disableLeaderboard(42L);
        assertEquals(BAD_REQUEST, resp.getStatusCode());
    }

    @Test
    public void testUpdateHighscoreDefault() {
        Player p = new Player("Fesing M", 1337);
        GameSession s = sut.addSession(new GameSession(GameSession.SessionType.SINGLEPLAYER, List.of(p))).getBody();
        assertNotNull(s);
        p.id = s.getPlayers().get(0).id;
        sut.updateHighscore(p, GameSession.SessionType.SINGLEPLAYER);
        assertEquals(1337, lbc.getPlayerById(p.id).getBody().bestSingleScore);
    }

    @Test
    public void testUpdateHighscoreTimeAttack() {
        Player p = new Player("Fesing M", 1337);
        GameSession s = sut.addSession(new GameSession(GameSession.SessionType.TIME_ATTACK, List.of(p))).getBody();
        assertNotNull(s);
        p.id = s.getPlayers().get(0).id;
        sut.updateHighscore(p, GameSession.SessionType.TIME_ATTACK);
        assertEquals(1337, lbc.getPlayerById(p.id).getBody().bestSingleScore);
    }

    @Test
    public void testUpdateHighscoreSurvival() {
        Player p = new Player("Fesing M", 1337);
        GameSession s = sut.addSession(new GameSession(GameSession.SessionType.SURVIVAL, List.of(p))).getBody();
        assertNotNull(s);
        p.id = s.getPlayers().get(0).id;
        sut.updateHighscore(p, GameSession.SessionType.SURVIVAL);
        assertEquals(1337, lbc.getPlayerById(p.id).getBody().bestSingleScore);
    }

    @Test
    public void testAddWaitingArea() {
        GameSession gs = new GameSession(GameSession.SessionType.WAITING_AREA, List.of(
                new Player("Fesing M", 1337)));

        var resp = sut.getSelectionRoomUpdates();
        resp.onCompletion(() -> assertEquals(gs, resp.getResult()));
        sut.addWaitingArea(gs);
    }

    @Test
    public void testChangeToMultiplayerSession() {
        GameSession gs = sut.addSession(new GameSession(GameSession.SessionType.WAITING_AREA)).getBody();
        assertNotNull(gs);
        gs.questionCounter = 0;
        sut.updateSession(gs);

        var resp = sut.getWaitingAreaUpdates(gs.id);
        resp.onCompletion(() -> {
            String msg = (String)resp.getResult();
            assertNotNull(msg);
            assertEquals("started: 0", msg);
        });

        var resp2 = sut.getSelectionRoomUpdates();
        resp2.onCompletion(() -> {
            GameSession sess = (GameSession) resp2.getResult();
            assertEquals(GameSession.SessionType.MULTIPLAYER, sess.sessionType);
            assertEquals(GameSession.SessionStatus.STARTED, sess.sessionStatus);
            assertEquals(1, sess.questionCounter);
        });
        sut.changeToMultiplayerSession(gs);
    }

    @Test
    public void testAddPlayerWaiting() {
        waiting = sut.addSession(waiting).getBody();
        assertNotNull(waiting);
        var resp = sut.getWaitingAreaUpdates(waiting.id);
        resp.onCompletion(() -> {
            String msg = (String)resp.getResult();
            assertEquals("addPlayer: beniGhost", msg);
        });
        sut.addPlayer(waiting.id, new Player("beniGhost", 1337));
    }

    @Test
    public void testRemovePlayerWaiting() {
        waiting = sut.addSession(waiting).getBody();
        assertNotNull(waiting);
        Player p  = sut.addPlayer(waiting.id, new Player("beniGhost", 1337)).getBody();
        assertNotNull(p);
        // buffer added so session doesn't get removed
        sut.addPlayer(waiting.id, new Player("buffer", 45));
        var resp = sut.getWaitingAreaUpdates(waiting.id);
        resp.onCompletion(() -> {
            String msg = (String)resp.getResult();
            assertEquals("removePlayer: beniGhost", msg);
        });
        sut.removePlayer(waiting.id, p.id);
    }

    @Test
    public void testGetRemovedPlayers() {
        first = sut.addSession(first).getBody();
        assertNotNull(first);
        Player p = sut.addPlayer(first.id, new Player("beniGhost", 1337)).getBody();
        assertNotNull(p);
        // buffer added so session doesn't get removed
        sut.addPlayer(first.id, new Player("buffer", 45));
        sut.removePlayer(first.id, p.id);
        assertEquals(List.of(p), sut.getRemovedPlayers(first.id).getBody());
    }

    @Test
    public void testRemoveLastPlayer() {
        first = sut.addSession(first).getBody();
        assertNotNull(first);
        Player p = sut.addPlayer(first.id, new Player("beniGhost", 1337)).getBody();
        assertNotNull(p);
        sut.removePlayer(first.id, p.id);
        assertEquals(BAD_REQUEST, sut.getSessionById(first.id).getStatusCode());
    }

    @Test
    public void testReadyWaiting() {
        waiting = sut.addSession(waiting).getBody();
        assertNotNull(waiting);
        Player p  = sut.addPlayer(waiting.id, new Player("beniGhost", 1337)).getBody();
        assertNotNull(p);
        // buffer added so session doesn't get started
        sut.addPlayer(waiting.id, new Player("buffer", 45));
        var resp = sut.getWaitingAreaUpdates(waiting.id);
        resp.onCompletion(() -> {
            String msg = (String)resp.getResult();
            assertEquals("playerReady: 1", msg);
        });
        sut.setPlayerReady(waiting.id);
    }

    @Test
    public void testUnreadyWaiting() {
        waiting = sut.addSession(waiting).getBody();
        assertNotNull(waiting);
        Player p  = sut.addPlayer(waiting.id, new Player("beniGhost", 1337)).getBody();
        assertNotNull(p);
        // buffer added so session doesn't get started
        sut.addPlayer(waiting.id, new Player("buffer", 45));
        sut.setPlayerReady(waiting.id);
        var resp = sut.getWaitingAreaUpdates(waiting.id);
        resp.onCompletion(() -> {
            String msg = (String)resp.getResult();
            assertEquals("playerReady: 0", msg);
        });
        sut.unsetPlayerReady(waiting.id);
    }

    @Test
    public void testGetJokerStates() {
        first = sut.addSession(first).getBody();
        assertNotNull(first);
        Player p = sut.addPlayer(first.id, new Player("big R", 10)).getBody();
        assertNotNull(p);
        sut.getJokerStates(first.id, p.id).getBody().forEach((k, v) -> assertEquals(Joker.JokerStatus.AVAILABLE, v));
    }

    @Test
    public void testUpdateJokers() {
        first = sut.addSession(first).getBody();
        assertNotNull(first);
        Player base = new Player("big R", 10);
        base.jokerStates.put("DoublePointsJoker", Joker.JokerStatus.USED_HOT);
        base.jokerStates.put("RemoveOneAnswerJoker", Joker.JokerStatus.USED);
        base.jokerStates.put("DecreaseTimeJoker", Joker.JokerStatus.AVAILABLE);
        base.previousEval = new Evaluation(2, Question.QuestionType.MULTIPLE_CHOICE, List.of(1L));

        Player p = sut.addPlayer(first.id, base).getBody();
        assertNotNull(p);
        sut.setPlayerReady(first.id);
        // Here we expect 2 since submit answer would add the score of 2 initially, and here we just check the doubling
        // we don't use submit answer here, so it doesn't add the other half of the points
        assertEquals(2, sut.getPlayers(first.id).getBody().get(0).currentPoints);
    }

    @Test
    public void testEndSessionMulti() {
        first = sut.addSession(first).getBody();
        assertNotNull(first);
        sut.setGameRounds(first.id, 1);
        Player base = new Player("Razvy", 1337);
        base.currentPoints = 10000;
        Player p = sut.addPlayer(first.id, base).getBody();
        assertNotNull(p);
        sut.setPlayerReady(first.id);
        first = sut.getSessionById(first.id).getBody();
        assertNotNull(first);
        assertEquals(0, first.playersReady.get());
        assertEquals(GameSession.SessionStatus.PAUSED, first.sessionStatus);
        assertEquals(10000, lbc.getPlayerById(p.id).getBody().bestMultiScore);
    }

    @Test
    public void testEndSessionSingle() {
        Player base = new Player("Razvy", 1337);
        base.currentPoints = 10000;
        GameSession gs = new GameSession(GameSession.SessionType.SINGLEPLAYER, List.of(base));
        gs = sut.addSession(gs).getBody();
        assertNotNull(gs);
        sut.setGameRounds(gs.id, 1);
        Player p = gs.getPlayers().get(0);
        sut.setPlayerReady(gs.id);
        assertEquals(BAD_REQUEST, sut.getSessionById(gs.id).getStatusCode());
        assertEquals(10000, lbc.getPlayerById(p.id).getBody().bestSingleScore);
    }

    @Test
    public void testAdvancePlayAgain() {
        Player base = new Player("Razvy", 1337);
        base.currentPoints = 10000;
        GameSession gs = new GameSession(GameSession.SessionType.MULTIPLAYER, List.of(base));
        gs.sessionStatus = GameSession.SessionStatus.PLAY_AGAIN;
        gs = sut.addSession(gs).getBody();
        assertNotNull(gs);
        sut.setGameRounds(gs.id, 1);
        sut.setPlayerReady(gs.id);
        gs = sut.getSessionById(gs.id).getBody();
        assertNotNull(gs);
        assertEquals(0, gs.questionCounter);
        gs.players.forEach(p -> assertEquals(0, p.currentPoints));
    }

    @Test
    public void testCreateEmptySession() {
        var actual = sut.addSession(first);
        assertEquals(OK, actual.getStatusCode());
    }

    @Test
    public void testUpdateSession() {
        var actual = sut.addSession(first);
        GameSession s = actual.getBody();

        GameSession next = new GameSession(GameSession.SessionType.MULTIPLAYER);
        next.id = s.id;
        next.playersReady.set(42);

        sut.updateSession(next);
        assertEquals(42, sut.getAllSessions().get(0).playersReady.get());
    }

    @Test
    public void testUpdateQuestion() {
        Question previousQuestion = first.currentQuestion;
        sut.updateQuestion(first);
        assertSame(1, first.questionCounter);
        assertNotNull(first.currentQuestion);
        assertNotSame(previousQuestion, first.currentQuestion);
    }

    @Test
    public void testPlayerAnswerMiddle() {
        Player p = new Player("test2", 0);
        first.addPlayer(p);
        sut.updateQuestion(first);
        Question tmp = first.currentQuestion;
        first.setPlayerReady();
        assertSame(1, first.questionCounter);
        assertSame(1, first.playersReady.get());
        assertEquals(tmp, first.currentQuestion);
    }

    @Test
    public void testPlayerAnswerFinal() {
        first.addPlayer(new Player("test", 0));
        sut.addSession(first);
        Question tmp = first.currentQuestion;
        sut.setPlayerReady(first.id);

        assertSame(2, first.questionCounter);
        assertSame(1, first.playersReady.get());
        assertNotNull(first.currentQuestion);
        assertNotSame(tmp, first.currentQuestion);
    }

    @Test
    public void getAllSessionsTest() {
        var sessions = sut.getAllSessions();
        assertTrue(sessions.size() == 0);

        sut.addSession(first);
        sessions = sut.getAllSessions();
        assertTrue(sessions.size() == 1);

        sut.addSession(new GameSession(GameSession.SessionType.MULTIPLAYER));
        sessions = sut.getAllSessions();
        assertTrue(sessions.size() == 2);

        assertEquals(1, sessions.get(0).id);
        assertEquals(2, sessions.get(1).id);
    }

    @Test
    public void getAvailableSessionsTest() {

        var newSession = sut.getAvailableSessions();
        // make sure fetch returns null if no sessions were added
        assertEquals(null, sut.getAvailableSessions().getBody());

        //make sure it does not fetch singleplayer rooms
        sut.addSession(singleplayer);
        assertEquals(null, sut.getAvailableSessions().getBody());

        sut.addSession(waiting);
        var availableSession = sut.getAvailableSessions().getBody();

        // make sure that a game session is returned successfully
        assertTrue(availableSession.get(0).getClass() == GameSession.class);
    }

    @Test
    public void getSessionTest() {
        sut.addSession(first);
        sut.addSession(new GameSession(GameSession.SessionType.MULTIPLAYER));

        //try to get an invalid session
        assertEquals(ResponseEntity.badRequest().build(), sut.getSessionById(42L));

        //make sure that it gets a valid session successfully
        assertNotEquals(ResponseEntity.badRequest().build(), sut.getSessionById(2L));

        //make sure that when getting a session it does not alter the session
        assertEquals(sut.getSessionById(1L).getBody(), first);
    }

    @Test
    public void addSessionTest() {
        sut.addSession(first);
        assertTrue(stubSessionManager.calledMethods.contains("save"));
        assertEquals(first, stubSessionManager.getValues().get(0));
    }

    @Test
    public void deleteSessionTest() {
        sut.addSession(first);
        var deletedSession = sut.removeSession(1);
        assertTrue(stubSessionManager.calledMethods.contains("delete"));
        assertEquals(0,
                stubSessionManager.getValues().stream().filter(e -> e.id == deletedSession.getBody().id).count());
    }

    @Test
    public void getPlayersTest() {
        sut.addSession(first);
        Player firstPlayer = sut.addPlayer(first.id, new Player("test", 0)).getBody();
        sut.addPlayer(first.id, new Player("test2", 0));
        assertTrue(sut.getPlayers(first.id).getBody().size() == 2);
        assertEquals(firstPlayer, sut.getPlayers(first.id).getBody().get(0));
    }

    @Test
    public void addPlayerTest() {
        sut.addSession(first);
        // player list is empty at first
        assertTrue(first.players.isEmpty());
        Player player = sut.addPlayer(first.id, new Player("test", 0)).getBody();

        // player list modified after operation
        assertTrue(first.players.size() != 0);

        // make sure the method refers to the same player
        assertEquals(first.players.get(0), player);
    }

    @Test
    public void removePlayerTest() {
        sut.addSession(first);
        Player firstPlayer = sut.addPlayer(first.id, new Player("test", 0)).getBody();
        sut.addPlayer(first.id, new Player("test2", 0));

        assertTrue(first.players.size() == 2);
        assertEquals(ResponseEntity.badRequest().build(), sut.removePlayer(10, 5));
        assertEquals(ResponseEntity.badRequest().build(), sut.removePlayer(0, 10));
        assertEquals(firstPlayer, sut.removePlayer(first.id, 1).getBody());
    }

    @Test
    public void setQuestionCounterTest() {
        first.setQuestionCounter(5);
        assertTrue(first.questionCounter == 5);
    }

    @Test
    public void updateTimeJokersTest() {
        sut.addSession(first);
        //Number of jokers is 0 at first
        assertTrue(first.timeJokers == 0);

        //make sure the number of jokers gets updated to 1
        sut.updateTimeJokers(first.id, 1);
        assertTrue(first.timeJokers == 1);
    }

    @Test
    public void setGameRoundsTest() {
        first.setGameRounds(10);
        assertTrue(first.gameRounds == 10);
    }

    @Test
    public void setPlayerReadyTest() {
        sut.addSession(first);
        sut.addPlayer(first.id, new Player("test", 0));
        sut.setPlayerReady(first.id);
        assertSame(1, sut.getSessionById(first.id).getBody().playersReady.get());

        ResponseEntity<GameSession> resp = sut.setPlayerReady(42L);
        assertEquals(HttpStatus.BAD_REQUEST, resp.getStatusCode());
    }

    @Test
    public void setPlayerReadyNoAdvanceTest() {
        sut.addSession(first);
        sut.addPlayer(first.id, new Player("test", 0));
        // buffer added so we don't progress to next round
        sut.addPlayer(first.id, new Player("buffer", 0));
        sut.setPlayerReady(first.id);
        assertSame(1, sut.getSessionById(first.id).getBody().playersReady.get());

        ResponseEntity<GameSession> resp = sut.setPlayerReady(42L);
        assertEquals(HttpStatus.BAD_REQUEST, resp.getStatusCode());
    }

    @Test
    public void unsetPlayerReadyTest() {
        sut.addSession(first);
        sut.addPlayer(first.id, new Player("test", 0));
        sut.setPlayerReady(first.id);
        sut.unsetPlayerReady(first.id);
        assertSame(0, sut.getSessionById(first.id).getBody().playersReady.get());

        ResponseEntity<GameSession> resp = sut.unsetPlayerReady(42L);
        assertEquals(HttpStatus.BAD_REQUEST, resp.getStatusCode());
    }

    @Test
    public void updateStatusTest() {
        sut.addSession(first);
        sut.updateStatus(first.id, GameSession.SessionStatus.ONGOING);
        assertEquals(GameSession.SessionStatus.ONGOING, sut.getSessionById(first.id).getBody().sessionStatus);

        ResponseEntity<GameSession> resp = sut.updateStatus(42L, GameSession.SessionStatus.ONGOING);
        assertEquals(HttpStatus.BAD_REQUEST, resp.getStatusCode());
    }

    @Test
    public void testAddJoker() {
        sut.addSession(first);

        assertTrue(first.usedJokers.isEmpty());

        Joker j = new Joker("test", "testJoker");
        Joker test = sut.addJoker(first.id, j).getBody();
        assertTrue(first.usedJokers.size() != 0);
        assertEquals(test, first.usedJokers.get(0));
    }

    @Test
    public void testGetAllJokers() {
        sut.addSession(first);
        assertEquals(0, sut.getAllJokers(first.id).getBody().size());
        Joker j1 = new Joker("test1", "testJoker1");
        Joker j2 = new Joker("test2", "testJoker2");
        Joker j3 = new Joker("test3", "testJoker3");
        sut.addJoker(first.id, j1);
        sut.addJoker(first.id, j2);
        sut.addJoker(first.id, j3);
        assertEquals(3, sut.getAllJokers(first.id).getBody().size());
        assertEquals(j1, sut.getAllJokers(first.id).getBody().get(0));
        assertEquals(j2, sut.getAllJokers(first.id).getBody().get(1));
        assertEquals(j3, sut.getAllJokers(first.id).getBody().get(2));
    }


    @SuppressWarnings("serial")
    public class MyRandom extends Random {

        public boolean wasCalled = false;

        @Override
        public int nextInt(int bound) {
            wasCalled = true;
            return nextInt;
        }
    }
}