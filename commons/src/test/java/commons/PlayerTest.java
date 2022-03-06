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

public class PlayerTest {

	@Test
	public void checkConstructor() {
		var p = new Player("test",0);
		assertEquals(new Player("test", 0), p);
	}

	@Test
	public void equalsHashCode() {
		var p = new Player("test",0);
		var p2 = new Player("test",0);
		assertEquals(p2, p2);
		assertEquals(p.hashCode(), p2.hashCode());
	}

	@Test
	public void notEqualsHashCode() {
		var p = new Player("test",0);
		var p2 = new Player("test2",0);
		assertNotEquals(p, p2);
		assertNotEquals(p.hashCode(), p2.hashCode());
	}

	@Test
	public void hasToString() {
		var str = new Player("test",0).toString();
		assertTrue(str.contains(Player.class.getSimpleName()));
		assertTrue(str.contains("\n"));
		assertTrue(str.contains("username"));
		assertTrue(str.contains("0"));
	}
}