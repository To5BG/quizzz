package server.api;

import commons.Activity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;


import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.http.HttpStatus.*;

public class ActivityControllerTest {

    private TestActivityRepository repo;

    private ActivityController sut;

    @BeforeEach
    public void setup() {
        repo = new TestActivityRepository();
        sut = new ActivityController(new Random(), repo);
    }

    @Test
    public void cannotAddNullActivityTest() {
        var actual = sut.addActivity(getActivity(null));
        assertEquals(BAD_REQUEST, actual.getStatusCode());
    }

    @Test
    public void getAllActivitiesTest() {
        Activity a = getActivity("test");
        sut.addActivity(a);
        var actual = sut.getAllActivities();
        assertEquals(List.of(a), actual);
    }

    @Test
    public void getOneActivityTest() {
        Activity activity = getActivity("test");
        sut.addActivity(activity);
        assertEquals(sut.getActivityById(1L).getBody(), activity);
    }

    @Test
    public void getInvalidActivityTest() {
        //sut.addActivity(getActivity("test"));
        var actual = sut.getActivityById(0L);
        assertEquals(actual.getStatusCode(), BAD_REQUEST);
    }

    @Test
    public void updateActivityTest() {
        Activity activity = getActivity("test");
        Activity other = getActivity("testtwo");
        sut.addActivity(activity);
        var actual = ResponseEntity.ok(sut.updateActivityById(1L, other)).getBody();
        other.id = 1L;
        assertEquals(other, actual.getBody());
    }

    @Test
    public void updateInvalidIdActivityTest() {
        Activity activity = getActivity("test");
        Activity other = getActivity("test2");
        sut.addActivity(activity);
        var actual = ResponseEntity.ok(sut.updateActivityById(42L, other)).getBody();
        assertEquals(BAD_REQUEST, actual.getStatusCode());
    }

    @Test
    public void updateInvalidUpdateActivityTest() {
        Activity activity = getActivity("test");
        Activity other = getActivity("");
        sut.addActivity(activity);
        var actual = ResponseEntity.ok(sut.updateActivityById(0L, other)).getBody();
        assertEquals(BAD_REQUEST, actual.getStatusCode());
    }

    @Test
    public void deleteActivityTest() {
        Activity activity = getActivity("test");
        sut.addActivity(activity);
        var actual = ResponseEntity.ok(sut.removeActivityById(1L)).getBody();
        assertEquals(NO_CONTENT, actual.getStatusCode());
    }

    @Test
    public void deleteInvalidActivityTest() {
        sut.addActivity(getActivity("test"));
        var actual = sut.removeActivityById(2L);
        assertEquals(actual.getStatusCode(), NOT_FOUND);
    }

    @Test
    public void databaseIsUsedTest() {
        sut.addActivity(getActivity("test"));
        repo.calledMethods.contains("save");
    }

    private static Activity getActivity(String str) {
        return (str == null)
                ? new Activity(null, null, null, null)
                : new Activity(str + " " + str + " " + str, "42", str, str);
    }
}