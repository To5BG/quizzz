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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import java.util.stream.Collectors;
import java.util.stream.Stream;

public class GameSessionTest {

	private static final Player SOME_PLAYER = new Player("test");

	@Test
	public void checkConstructor() {
		var s = new GameSession(Stream.of(SOME_PLAYER).collect(Collectors.toList()));
		assertEquals(SOME_PLAYER, s.players.get(0));
	}

	@Test
	public void equalsHashCode() {
		var a = new GameSession(Stream.of(new Player("blah")).collect(Collectors.toList()));
		var b = new GameSession(Stream.of(new Player("blah")).collect(Collectors.toList()));
		assertEquals(a, b);
		assertEquals(a.hashCode(), b.hashCode());
	}

	@Test
	public void notEqualsHashCode() {
		var a = new GameSession(Stream.of(new Player("blah")).collect(Collectors.toList()));
		var b = new GameSession(Stream.of(new Player("blahh")).collect(Collectors.toList()));
		assertNotEquals(a, b);
		assertNotEquals(a.hashCode(), b.hashCode());
	}

	@Test
	public void hasToString() {
		var str = new GameSession(Stream.of(new Player("blah")).collect(Collectors.toList())).toString();
		assertTrue(str.contains(GameSession.class.getSimpleName()));
		assertTrue(str.contains("\n"));
		assertTrue(str.contains("player"));
	}
}