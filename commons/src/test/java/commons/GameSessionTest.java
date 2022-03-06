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

import org.junit.jupiter.api.Test;

import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

public class GameSessionTest {

    private static final Player SOME_PLAYER = new Player("test",0);

    @Test
    public void checkConstructor() {
        var s = new GameSession("multiplayer", Stream.of(SOME_PLAYER).collect(Collectors.toList()));
        assertEquals(SOME_PLAYER, s.players.get(0));
        assertNotNull(s.expectedAnswers);
        assertSame(1, s.questionCounter);
        assertSame(0, s.playersReady);
    }

    @Test
    public void testAddPlayer() {
        GameSession s = new GameSession("multiplayer",
                Stream.of(SOME_PLAYER).collect(Collectors.toList()));
        Player p = new Player("test2",0);
        s.addPlayer(p);
        assertSame(2, s.players.size());
        assertEquals(p, s.players.get(1));
    }

    @Test
    public void testRemovePlayer() {
        GameSession s = new GameSession("multiplayer",
                Stream.of(SOME_PLAYER).collect(Collectors.toList()));
        s.removePlayer(SOME_PLAYER);
        assertSame(0, s.players.size());
    }

    @Test
    public void testUpdateQuestion() {
        GameSession s = new GameSession("multiplayer",
                Stream.of(SOME_PLAYER).collect(Collectors.toList()));
        s.updateQuestion();
        assertSame(1, s.expectedAnswers.size());
        assertSame(2, s.questionCounter);
        assertNotNull(s.currentQuestion);
        assertEquals("Question #1", s.currentQuestion.prompt);
    }

    @Test
    public void testPlayerAnswerMiddle() {
        GameSession s = new GameSession("multiplayer",
                Stream.of(SOME_PLAYER).collect(Collectors.toList()));
        Player p = new Player("test2",0);
        s.addPlayer(p);
        s.updateQuestion();
        Question tmp = s.currentQuestion;
        s.setPlayerReady();
        assertSame(2, s.questionCounter);
        assertSame(1, s.playersReady);
        assertEquals(tmp, s.currentQuestion);
    }

    @Test
    public void testPlayerAnswerFinal() {
        GameSession s = new GameSession("multiplayer",
                Stream.of(SOME_PLAYER).collect(Collectors.toList()));
        s.updateQuestion();
        Question tmp = s.currentQuestion;
        s.setPlayerReady();
        assertSame(3, s.questionCounter);
        assertSame(1, s.playersReady);
        assertSame(1, s.expectedAnswers.size());
        assertNotNull(s.currentQuestion);
        assertNotEquals(tmp, s.currentQuestion);
        assertEquals("Question #2", s.currentQuestion.prompt);
    }

    @Test
    public void testEquals() {
        var a = new GameSession("multiplayer", Stream.of(new Player("blah",0))
                .collect(Collectors.toList()));
        var b = new GameSession("multiplayer", Stream.of(new Player("blah",0))
                .collect(Collectors.toList()));
        var c = new GameSession("multiplayer",
                Stream.of(new Player("blahhh",0)).collect(Collectors.toList()));

        assertEquals(a, a);
        assertEquals(a, b);
        assertNotEquals(a, c);
    }

    @Test
    public void testHashCode() {
        var a = new GameSession("multiplayer", Stream.of(new Player("blah",0))
                .collect(Collectors.toList()));
        var b = new GameSession("multiplayer", Stream.of(new Player("blah",0))
                .collect(Collectors.toList()));
        assertEquals(a.hashCode(), a.hashCode());
        assertEquals(a.hashCode(), b.hashCode());
    }

    @Test
    public void testToString() {
        var s = new GameSession("multiplayer", Stream.of(new Player("blah",0))
                .collect(Collectors.toList()));
        String str = s.toString();

        System.out.println(str);
        assertTrue(str.contains(GameSession.class.getSimpleName()));
        assertTrue(str.contains("username=blah"));
        assertTrue(str.contains("questionCounter=1"));
        assertTrue(str.contains("playersReady=0"));
        assertTrue(str.contains("expectedAnswers=[]"));
        assertTrue(str.contains("currentQuestion=<null>"));
        assertTrue(str.contains(s.players.get(0).toString()));
    }
}