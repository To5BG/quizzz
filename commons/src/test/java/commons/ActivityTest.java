package commons;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ActivityTest {

    @Test
    public void checkEmptyConstructor() {
        Activity a = new Activity();
        assertNull(a.title);
        assertEquals(0L, a.consumption_in_wh);
        assertNull(a.image_path);
        assertNull(a.source);
    }

    @Test
    public void checkConstructor() {
        var a = new Activity("t", 1L, "root", "google.com");
        assertEquals("t", a.title);
        assertEquals(1L, a.consumption_in_wh);
        assertEquals("root", a.image_path);
        assertEquals("google.com", a.source);
    }

    @Test
    public void equalsHashCode() {
        var a = new Activity("t", 1L, "root", "google.com");
        var b = new Activity("t", 1L, "root", "google.com");
        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
    }

    @Test
    public void hasToString() {
        var actual = new Activity("t", 1L, "root", "google.com").toString();
        assertTrue(actual.contains(Activity.class.getSimpleName()));

        assertTrue(actual.contains("consumption_in_wh=1"));
        assertTrue(actual.contains("id=0"));
        assertTrue(actual.contains("image_path=root"));
        assertTrue(actual.contains("source=google.com"));
        assertTrue(actual.contains("title=t"));
    }


}