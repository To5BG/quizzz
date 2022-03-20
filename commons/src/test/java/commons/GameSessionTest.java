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
package commons;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

public class GameSessionTest {

    private static final Player SOME_PLAYER = new Player("test",0);
    private static GameSession s = null;

    @BeforeEach
    public void setup() {
        s = new GameSession(GameSession.SessionType.MULTIPLAYER,
                Stream.of(SOME_PLAYER).collect(Collectors.toList()));
    }

    @Test
    public void testEmptyConstructor() {
        GameSession session = new GameSession();
        assertNull(session.expectedAnswers);
        assertNull(session.currentQuestion);
        assertNull(session.players);
        assertNull(session.sessionStatus);
        assertNull(session.sessionType);
        assertSame(0, session.playersReady);
        assertSame(0, session.questionCounter);
        assertSame(0L, session.id);
    }

    @Test
    public void testSetReadyWaitingArea() {
        GameSession waitingArea = new GameSession(GameSession.SessionType.WAITING_AREA);
        waitingArea.addPlayer(SOME_PLAYER);
        waitingArea.setPlayerReady();
        assertSame(1, waitingArea.playersReady);
        waitingArea.setPlayerReady();
        assertSame(1, waitingArea.playersReady);
    }

    @Test
    public void testUnsetReady() {
        s.unsetPlayerReady();
        assertSame(0, s.playersReady);
        s.setPlayerReady();
        s.unsetPlayerReady();
        assertSame(0, s.playersReady);
    }

    @Test
    public void testSetCurrentQuestion() {
        Question q = new Question("Question 1", "img1", Question.QuestionType.MULTIPLE_CHOICE);
        s.setCurrentQuestion(q);
        assertEquals(q, s.currentQuestion);
    }

    @Test
    public void testSetSessionStatus() {
        s.setSessionStatus(GameSession.SessionStatus.ONGOING);
        assertEquals(GameSession.SessionStatus.ONGOING, s.sessionStatus);
    }

    @Test
    public void checkConstructor() {
        assertEquals(SOME_PLAYER, s.players.get(0));
        assertNotNull(s.expectedAnswers);
        assertSame(0, s.questionCounter);
        assertSame(0, s.playersReady);
    }

    @Test
    public void testAddPlayer() {
        Player p = new Player("test2",0);
        s.addPlayer(p);
        assertSame(2, s.players.size());
        assertEquals(p, s.players.get(1));
    }

    @Test
    public void testRemovePlayer() {
        s.removePlayer(SOME_PLAYER);
        assertSame(0, s.players.size());
    }

    @Test
    public void testGetPlayers() {
        GameSession s = new GameSession(GameSession.SessionType.MULTIPLAYER,
                Stream.of(SOME_PLAYER).collect(Collectors.toList()));
        Player a = new Player("abc", 0);
        Player b = new Player("def", 0);
        s.addPlayer(a);
        s.addPlayer(b);
        List<Player> players = s.getPlayers();
        assertSame(3, players.size());
        assertEquals(a, players.get(1));
        assertEquals(b, players.get(2));
    }

    @Test
    public void getTimeJokersTest() {
        assertTrue(s.getTimeJokers() == 0);
    }

    @Test
    public void setTimeJokersTest() {
        s.setTimeJokers(2);
        assertTrue(s.getTimeJokers() == 2);
    }

    @Test
    public void testEquals() {
        var s2 = new GameSession(GameSession.SessionType.MULTIPLAYER,
                Stream.of(new Player("blah",0))
                        .collect(Collectors.toList()));

        var s3 = new GameSession(GameSession.SessionType.MULTIPLAYER,
                Stream.of(new Player("blah",0))
                        .collect(Collectors.toList()));

        var s4 = new GameSession(GameSession.SessionType.MULTIPLAYER,
                Stream.of(new Player("blahhh",0))
                        .collect(Collectors.toList()));

        assertEquals(s2, s2);
        assertEquals(s2, s3);
        assertNotEquals(s, s4);
    }

    @Test
    public void testHashCode() {
        var s2 = new GameSession(GameSession.SessionType.MULTIPLAYER,
                Stream.of(new Player("blah",0))
                        .collect(Collectors.toList()));

        var s3 = new GameSession(GameSession.SessionType.MULTIPLAYER,
                Stream.of(new Player("blah",0))
                        .collect(Collectors.toList()));

        assertEquals(s2.hashCode(), s2.hashCode());
        assertEquals(s3.hashCode(), s3.hashCode());
    }

    @Test
    public void testToString() {
        var s = new GameSession(GameSession.SessionType.MULTIPLAYER,
                Stream.of(new Player("blah",0))
                        .collect(Collectors.toList()));
        String str = s.toString();

        System.out.println(str);
        assertTrue(str.contains(GameSession.class.getSimpleName()));
        assertTrue(str.contains("username=blah"));
        assertTrue(str.contains("questionCounter=0"));
        assertTrue(str.contains("playersReady=0"));
        assertTrue(str.contains("expectedAnswers=[]"));
        assertTrue(str.contains("currentQuestion=<null>"));
        assertTrue(str.contains(s.players.get(0).toString()));
    }
}