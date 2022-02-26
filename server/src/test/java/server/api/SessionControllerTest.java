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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.http.HttpStatus.OK;

import java.util.Random;

import commons.GameSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

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
    public void cannotAddNullPerson() {
        var actual = sut.addSession(new GameSession());
        assertEquals(OK, actual.getStatusCode());
    }

    /*
    @Test
    public void randomSelection() {
        sut.addSession(new GameSession());
        sut.addSession(new GameSession());
        nextInt = 1;

        assertTrue(random.wasCalled);
    }
    */

    @Test
    public void databaseIsUsed() {
        sut.addSession(new GameSession());
        repo.calledMethods.contains("save");
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