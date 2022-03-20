package commons;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class EmojiTest {
    @Test
    public void testEmptyConstructor() {
        Emoji e = new Emoji();
        assertNull(e.username);
        assertEquals(Emoji.EmojiType.UNKNOWN, e.emoji);
    }

    @Test
    public void testConstructor() {
        Emoji e = new Emoji("test", Emoji.EmojiType.FUNNY);
        assertEquals("test", e.username);
        assertEquals(Emoji.EmojiType.FUNNY, e.emoji);
    }

    @Test
    public void testEquals() {
        Emoji e1 = new Emoji("test", Emoji.EmojiType.FUNNY);
        Emoji e2 = new Emoji("test", Emoji.EmojiType.FUNNY);
        Emoji e3 = new Emoji("test2", Emoji.EmojiType.SAD);

        assertEquals(e1, e1);
        assertEquals(e1, e2);
        assertNotEquals(e3, e1);
    }

    @Test
    public void testHashCode() {
        Emoji e1 = new Emoji("test", Emoji.EmojiType.FUNNY);
        Emoji e2 = new Emoji("test", Emoji.EmojiType.FUNNY);

        assertEquals(e1.hashCode(), e1.hashCode());
        assertEquals(e1.hashCode(), e2.hashCode());
    }

    @Test
    public void testToString() {
        Emoji e1 = new Emoji("test", Emoji.EmojiType.FUNNY);
        String result = e1.toString();

        assertTrue(result.contains(Emoji.class.getSimpleName()));
        assertTrue(result.contains("username=test"));
        assertTrue(result.contains("emoji=FUNNY"));
    }
}
