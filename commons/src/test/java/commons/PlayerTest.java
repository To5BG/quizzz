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

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class PlayerTest {

	@Test
	public void checkEmptyConstructor() {
		Player p = new Player();
		assertSame(0, p.currentPoints);
		assertNull(p.username);
		assertNull(p.ans);
	}

	@Test
	public void checkConstructor() {
		var p = new Player("test",0);
		assertEquals(new Player("test", 0), p);
	}

	@Test
	public void testSetCurrentPoint() {
		var p = new Player("abc", 0);
		p.setCurrentPoints(8);
		assertEquals(8, p.currentPoints);
	}

	@Test
	public void testSetBestPoint() {
		var p = new Player("abc", 0);
		p.setBestSingleScore(8);
		assertEquals(8, p.bestSingleScore);
	}

	@Test
	public void equalsHashCodeTest() {
		var p = new Player("test",0);
		var p2 = new Player("test",0);
		assertEquals(p2, p2);
		assertEquals(p.hashCode(), p2.hashCode());
	}

	@Test
	public void notEqualsHashCodeTest() {
		var p = new Player("test",0);
		var p2 = new Player("test2",0);
		assertNotEquals(p, p2);
		assertNotEquals(p.hashCode(), p2.hashCode());
	}

	@Test
	public void hasToStringTest() {
		var str = new Player("test",0).toString();
		assertTrue(str.contains(Player.class.getSimpleName()));
		assertTrue(str.contains("\n"));
		assertTrue(str.contains("username"));
		assertTrue(str.contains("0"));
	}

	@Test
	public void setAnswerTest() {
		Player p = new Player("test", 0);
		Answer a = new Answer(List.of(1, 2, 3), Question.QuestionType.MULTIPLE_CHOICE);
		p.setAnswer(a);
		String expected = "[" + System.lineSeparator() + "  answers=[1, 2, 3]" +
				System.lineSeparator() + "  type=MULTIPLE_CHOICE" +
				System.lineSeparator() + "]";
		assertEquals(expected, p.ans);
	}

	@Test
	public void parseAnswerTest() {
		Player p = new Player("test", 0);
		Answer a = new Answer(List.of(1, 2, 3), Question.QuestionType.MULTIPLE_CHOICE);
		p.setAnswer(a);
		assertEquals(a, p.parsedAnswer());

		Answer b = new Answer(List.of(1), Question.QuestionType.UNKNOWN);
		p.setAnswer(b);
		assertEquals(b, p.parsedAnswer());

		Answer c = new Answer(List.of(), Question.QuestionType.UNKNOWN);
		p.setAnswer(c);
		assertEquals(c, p.parsedAnswer());
	}
}