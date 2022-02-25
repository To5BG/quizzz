package server.api;

import commons.Activity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;


import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.http.HttpStatus.BAD_REQUEST;

public class ActivityControllerTest {

    private TestActivityRepository repo;

    private ActivityController sut;

    @BeforeEach
    public void setup() {
        repo = new TestActivityRepository();
        sut = new ActivityController(repo);
    }

    @Test
    public void cannotAddNullActivity() {
        var actual = sut.add(getActivity(null));
        assertEquals(BAD_REQUEST, actual.getStatusCode());
    }

    @Test
    public void getOneActivity() {
        sut.add(getActivity("a1"));
        var actual = ResponseEntity.ok(repo.getById((long) 0));
        assertEquals("a1", actual.getBody().title);
    }

    @Test
    public void databaseIsUsed() {
        sut.add(getActivity("a1"));
        repo.calledMethods.contains("save");
    }

    private static Activity getActivity(String str) {
        return new Activity(str, str, str, str);
    }
}