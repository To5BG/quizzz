package commons;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class JokerTest {
    @Test
    public void testConstructor() {
        Joker j = new Joker("test", "testJoker");
        assertEquals("test", j.username());
        assertEquals("testJoker", j.jokerName());
    }

    @Test
    public void testEquals() {
        Joker j1 = new Joker("test", "jokerName");
        Joker j2 = new Joker("test", "jokerName");
        Joker j3 = new Joker("test2", "jokerName2");

        assertEquals(j1, j1);
        assertEquals(j1, j2);
        assertNotEquals(j3, j1);
    }

    @Test
    public void testHashCode() {
        Joker j1 = new Joker("test", "jokerName");
        Joker j2 = new Joker("test", "jokerName");

        assertEquals(j1.hashCode(), j1.hashCode());
        assertEquals(j1.hashCode(), j2.hashCode());
    }

    @Test
    public void testToString() {
        Joker j1 = new Joker("test", "jokerName");
        String result = j1.toString();

        assertTrue(result.contains(Joker.class.getSimpleName()));
        assertTrue(result.contains("username=test"));
        assertTrue(result.contains("jokerName=jokerName"));
    }
}
