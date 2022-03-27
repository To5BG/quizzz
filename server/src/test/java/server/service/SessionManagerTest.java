package server.service;

import commons.GameSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SessionManagerTest {

    private SessionManager sut;
    private GameSession first;

    @BeforeEach
    public void setup() {
        sut = new SessionManager();
        first = new GameSession(GameSession.SessionType.MULTIPLAYER);
    }

    //Testing getters for coverage
    @Test
    public void getValuesTest() {
        assertEquals(0, sut.getValues().size());
        sut.save(first);
        assertEquals(1, sut.getValues().size());
        assertTrue(sut.getValues().contains(first));
    }

    @Test
    public void getCounter() {
        assertEquals(1, sut.getCounter());
        sut.save(first);
        assertEquals(2, sut.getCounter());
    }

    @Test
    public void saveTest() {
        sut.save(first);
        assertTrue(sut.getCounter() == 2L);
        assertTrue(sut.getValues().size() == 1);
        // prevent entry duplication
        sut.save(first);
        assertTrue(sut.getCounter() == 2L);
        assertTrue(sut.getValues().size() == 1);
    }

    @Test
    public void deleteTest() {
        sut.save(first);
        sut.delete(100L);
        assertTrue(sut.getValues().size() == 1);
        assertTrue(sut.getCounter() == 2L);
        sut.delete(1L);
        assertTrue(sut.getValues().size() == 0);
        assertTrue(sut.getCounter() == 2L);
    }

    @Test
    public void getByIdTest() {
        assertNull(sut.getById(1L));
        sut.save(first);
        assertEquals(first, sut.getById(1L));
    }

    @Test
    public void isValidTest() {
        assertFalse(sut.isValid(10L));
        assertFalse(sut.isValid(-1L));
        sut.save(first);
        assertTrue(sut.isValid(1L));
    }
}