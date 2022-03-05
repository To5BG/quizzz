package server.api;

import commons.Activity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;


import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.http.HttpStatus.*;

public class ActivityControllerTest {

    private TestActivityRepository repo;

    private ActivityController sut;

    @BeforeEach
    public void setup() {
        repo = new TestActivityRepository();
        sut = new ActivityController(repo);
    }

    @Test
    public void cannotAddNullActivityTest() {
        var actual = sut.addActivity(getActivity(null));
        assertEquals(BAD_REQUEST, actual.getStatusCode());
    }

    @Test
    public void getAllActivitiesTest() {
        sut.addActivity(getActivity("a1"));
        var actual = ResponseEntity.ok(sut.getAllActivities());
        assertTrue(List.of(getActivity("a1")).equals(actual.getBody()));
    }

    @Test
    public void getOneActivityTest() {
        Activity activity = getActivity("a1");
        sut.addActivity(activity);
        assertEquals(sut.getActivityById(0L).getBody(), activity);
    }

    @Test
    public void getInvalidActivityTest() {
        //sut.addActivity(getActivity("a1"));
        var actual = sut.getActivityById(0L);
        assertEquals(actual.getStatusCode(), BAD_REQUEST);
    }

    @Test
    public void updateActivityTest() {
        Activity activity = getActivity("a1");
        Activity other = getActivity("a2");
        sut.addActivity(activity);
        var actual = ResponseEntity.ok(sut.updateActivityById(0L, other)).getBody();
        assertEquals(other, actual.getBody());
    }

    @Test
    public void updateInvalidIdActivityTest() {
        Activity activity = getActivity("a1");
        Activity other = getActivity("a2");
        sut.addActivity(activity);
        var actual = ResponseEntity.ok(sut.updateActivityById(42L, other)).getBody();
        assertEquals(BAD_REQUEST, actual.getStatusCode());
    }

    @Test
    public void updateInvalidUpdateActivityTest() {
        Activity activity = getActivity("a1");
        Activity other = getActivity("");
        sut.addActivity(activity);
        var actual = ResponseEntity.ok(sut.updateActivityById(0L, other)).getBody();
        assertEquals(BAD_REQUEST, actual.getStatusCode());
    }

    @Test
    public void deleteActivityTest() {
        Activity activity = getActivity("a1");
        sut.addActivity(activity);
        var actual = ResponseEntity.ok(sut.removeActivityById(0L)).getBody();
        assertEquals(NO_CONTENT, actual.getStatusCode());
    }

    @Test
    public void deleteInvalidActivityTest() {
        sut.addActivity(getActivity("a1"));
        var actual = sut.removeActivityById(2L);
        assertEquals(actual.getStatusCode(), NOT_FOUND);
    }

    @Test
    public void databaseIsUsedTest() {
        sut.addActivity(getActivity("a1"));
        repo.calledMethods.contains("save");
    }

    private static Activity getActivity(String str) {
        return new Activity(str, str, str, str);
    }
}