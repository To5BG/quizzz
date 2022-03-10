package server.api;

import commons.Activity;
import server.database.ActivityRepository;

public class TestActivityRepository extends StubRepository<Activity, Long> implements ActivityRepository {
    public TestActivityRepository() {
        super(Activity.class);
    }
}
