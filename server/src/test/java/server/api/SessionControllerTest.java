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

import java.util.ArrayList;
import java.util.Random;

import commons.GameSession;
import commons.Player;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

public class SessionControllerTest {

    public int nextInt;
    private MyRandom random;
    private TestGameSessionRepository repo;

    private SessionController sut;

    @BeforeEach
    public void setup() {
        random = new MyRandom();
        repo = new TestGameSessionRepository();
        sut = new SessionController(random, repo);
    }

    @Test
    public void createEmptySession() {
        var actual = sut.addSession(new GameSession(new ArrayList<>()));
        assertEquals(OK, actual.getStatusCode());
    }

    @Test
    public void getAllSessionsTest() {
        var sessions = sut.getAllSessions();
        assertTrue(sessions.size() == 0);

        sut.addSession(new GameSession(new ArrayList<>()));
        assertTrue(sessions.size() == 1);

        // Instead of defaulting the arraylist in a new constructor a new array is passed every time
        // due to the comment below the default constructor that says "for object mapper"
        sut.addSession(new GameSession(new ArrayList<>()));
        assertTrue(sessions.size() == 2);

        assertEquals(0, sessions.get(0).id);
        assertEquals(1, sessions.get(1).id);
    }

    @Test
    public void getAvailableSessionTest() {

        var newSession = sut.getAvailableSession();
        // make sure that not a new session is created, since one is already available
        assertEquals(sut.getAvailableSession(), newSession);

        sut.addSession(new GameSession(new ArrayList<>()));
        var availableSession = sut.getAvailableSession().getBody();

        // make sure that a game session is returned successfully
        assertTrue(availableSession.getClass() == GameSession.class);

        // make sure that the returned available session is not different from the new session
        assertEquals(availableSession, newSession.getBody());
    }

    @Test
    public void getSessionTest() {
        GameSession firstSession = sut.addSession(new GameSession(new ArrayList<>())).getBody();
        sut.addSession(new GameSession(new ArrayList<>()));

        //try to get an invalid session
        assertEquals(ResponseEntity.badRequest().build(), sut.getSessionById(42L));

        //make sure that it gets a valid session successfully
        assertNotEquals(ResponseEntity.badRequest().build(), sut.getSessionById(1L));

        //make sure that when getting a session it does not alter the session
        assertEquals(sut.getSessionById(0L).getBody(), firstSession);
    }

    @Test
    public void addSessionTest() {
        var savedSession = sut.addSession(new GameSession(new ArrayList<>())).getBody();
        repo.calledMethods.contains("save");
        assertEquals(savedSession, repo.findAll().get(0));
    }

    @Test
    public void deleteSessionTest() {
        /*
        var deletedSession = sut.removeSession(1).getBody();
        repo.calledMethods.contains("delete");
        assertTrue(repo.existsById(deletedSession.id));
         */
    }

    @Test
    public void getPlayersTest() {
        GameSession session = sut.addSession(new GameSession(new ArrayList<>())).getBody();
        Player firstPlayer = sut.addPlayer(session.id, new Player("test")).getBody();
        sut.addPlayer(session.id, new Player("test2"));
        assertTrue(sut.getPlayers(session.id).getBody().size() == 2);
        assertEquals(firstPlayer, sut.getPlayers(session.id).getBody().get(0));
    }

    @Test
    public void addPlayerTest() {
        GameSession session = sut.addSession(new GameSession(new ArrayList<>())).getBody();
        // player list is empty at first
        assertTrue(session.players.isEmpty());
        Player player = sut.addPlayer(0, new Player("test")).getBody();

        // player list modified after operation
        assertTrue(session.players.size() != 0);

        // make sure the method refers to the same player
        assertEquals(session.players.get(0), player);
    }

    @Test
    public void removePlayerTest() {
        GameSession session = sut.addSession(new GameSession(new ArrayList<>())).getBody();
        Player firstPlayer = sut.addPlayer(session.id, new Player("test")).getBody();
        sut.addPlayer(session.id, new Player("test2"));

        assertTrue(session.players.size() == 2);
        assertEquals(ResponseEntity.badRequest().build(), sut.removePlayer(10, 5));
        assertEquals(ResponseEntity.badRequest().build(), sut.removePlayer(0, 10));
        assertEquals(firstPlayer, sut.removePlayer(session.id, 0).getBody());
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