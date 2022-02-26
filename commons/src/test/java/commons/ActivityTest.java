package commons;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ActivityTest {
    @Test
    public void checkConstructor() {
        var a = new Activity("t", "1", "root", "google.com");
        assertEquals("t", a.title);
        assertEquals("1", a.consumption);
        assertEquals("root", a.imagePath);
        assertEquals("google.com", a.source);
    }

    @Test
    public void equalsHashCode() {
        var a = new Activity("t", "1", "root", "google.com");
        var b = new Activity("t", "1", "root", "google.com");
        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
    }

    @Test
    public void notEqualsHashCode() {
        var a = new Activity("t", "1", "root", "google.com");
        var b = new Activity("notT", "2", "root", "google.com");
        assertNotEquals(a, b);
        assertNotEquals(a.hashCode(), b.hashCode());
    }

    @Test
    public void hasToString() {
        var actual = new Activity("t", "1", "root", "google.com").toString();
        assertTrue(actual.contains(Activity.class.getSimpleName()));

        assertTrue(actual.contains("[\r\n" +
                "  consumption=1\r\n" +
                "  id=0\r\n" +
                "  imagePath=root\r\n" +
                "  source=google.com\r\n" +
                "  title=t\r\n" +
                "]"));
    }


}