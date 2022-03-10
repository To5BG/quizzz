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
import static org.springframework.http.HttpStatus.OK;

import java.util.List;
import java.util.Random;

import commons.Answer;
import commons.GameSession;
import commons.Player;
import commons.Question;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

public class SessionControllerTest {

    public int nextInt;
    private MyRandom random;
    private TestGameSessionRepository repo;

    private SessionController sut;
    private GameSession first;

    @BeforeEach
    public void setup() {
        random = new MyRandom();
        repo = new TestGameSessionRepository();
        sut = new SessionController(random, repo, "test");
        first = new GameSession(GameSession.SessionType.MULTIPLAYER);
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
        next.playersReady = 42;

        sut.updateSession(next);
        assertEquals(42, repo.findAll().get(0).playersReady);
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
    public void getAvailableSessionTest() {

        var newSession = sut.getAvailableSession();
        // make sure fetch returns null if no sessions were added
        assertEquals(sut.getAvailableSession().getBody(), null);

        sut.addSession(first);
        var availableSession = sut.getAvailableSession().getBody();

        // make sure that a game session is returned successfully
        assertTrue(availableSession.getClass() == GameSession.class);
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
        assertTrue(repo.calledMethods.contains("save"));
        assertEquals(first, repo.findAll().get(0));
    }

    @Test
    public void deleteSessionTest() {
        sut.addSession(first);
        var deletedSession = sut.removeSession(1);
        assertTrue(repo.calledMethods.contains("delete"));
        assertFalse(repo.existsById(deletedSession.getBody().id));
    }

    @Test
    public void getPlayersTest() {
        sut.addSession(first);
        Player firstPlayer = sut.addPlayer(first.id, new Player("test",0)).getBody();
        sut.addPlayer(first.id, new Player("test2",0));
        assertTrue(sut.getPlayers(first.id).getBody().size() == 2);
        assertEquals(firstPlayer, sut.getPlayers(first.id).getBody().get(0));
    }

    @Test
    public void addPlayerTest() {
        sut.addSession(first);
        // player list is empty at first
        assertTrue(first.players.isEmpty());
        Player player = sut.addPlayer(first.id, new Player("test",0)).getBody();

        // player list modified after operation
        assertTrue(first.players.size() != 0);

        // make sure the method refers to the same player
        assertEquals(first.players.get(0), player);
    }

    @Test
    public void removePlayerTest() {
        sut.addSession(first);
        Player firstPlayer = sut.addPlayer(first.id, new Player("test",0)).getBody();
        sut.addPlayer(first.id, new Player("test2",0));

        assertTrue(first.players.size() == 2);
        assertEquals(ResponseEntity.badRequest().build(), sut.removePlayer(10, 5));
        assertEquals(ResponseEntity.badRequest().build(), sut.removePlayer(0, 10));
        assertEquals(firstPlayer, sut.removePlayer(first.id, 0).getBody());
    }

    @Test
    public void setPlayerReadyTest() {
        sut.addSession(first);
        sut.addPlayer(first.id, new Player("test", 0));
        sut.setPlayerReady(first.id);
        assertSame(1, sut.getSessionById(first.id).getBody().playersReady);

        ResponseEntity<GameSession> resp = sut.setPlayerReady(42L);
        assertEquals(HttpStatus.BAD_REQUEST, resp.getStatusCode());
    }

    @Test
    public void unsetPlayerReadyTest() {
        sut.addSession(first);
        sut.addPlayer(first.id, new Player("test", 0));
        sut.setPlayerReady(first.id);
        sut.unsetPlayerReady(first.id);
        assertSame(0, sut.getSessionById(first.id).getBody().playersReady);

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
    public void setAnswerTest() {
        sut.addSession(first);
        ResponseEntity<Player> p = sut.addPlayer(first.id, new Player("test", 0));
        assertNotNull(p.getBody());
        Answer a = new Answer(List.of(1, 2, 3), Question.QuestionType.MULTIPLE_CHOICE);
        sut.setAnswer(first.id, p.getBody().id, a);

        ResponseEntity<GameSession> sess = sut.getSessionById(first.id);
        assertNotNull(sess.getBody());
        Player player = sess.getBody().players.get(0);
        assertEquals(a, player.parsedAnswer());

        ResponseEntity<Answer> resp = sut.setAnswer(first.id, 42L, a);
        assertEquals(HttpStatus.BAD_REQUEST, resp.getStatusCode());

        resp = sut.setAnswer(42L, 42L, a);
        assertEquals(HttpStatus.BAD_REQUEST, resp.getStatusCode());
    }

    @Test
    public void getPlayerAnswerTest() {
        sut.addSession(first);
        ResponseEntity<Player> p = sut.addPlayer(first.id, new Player("test", 0));
        assertNotNull(p.getBody());
        Answer a = new Answer(List.of(1, 2, 3), Question.QuestionType.MULTIPLE_CHOICE);
        sut.setAnswer(first.id, p.getBody().id, a);

        ResponseEntity<Answer> ans = sut.getPlayerAnswer(first.id, p.getBody().id);
        assertNotNull(ans.getBody());
        assertEquals(a, ans.getBody());

        ResponseEntity<Answer> resp = sut.getPlayerAnswer(first.id, 42L);
        assertEquals(HttpStatus.BAD_REQUEST, resp.getStatusCode());

        resp = sut.getPlayerAnswer(42L, 42L);
        assertEquals(HttpStatus.BAD_REQUEST, resp.getStatusCode());
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