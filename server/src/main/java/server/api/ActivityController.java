package server.api;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import commons.Activity;
import server.database.ActivityRepository;

@RestController
@RequestMapping("/api/activities")
public class ActivityController {

    private final ActivityRepository repo;

    /**
     * Constructor method
     * @param repo - ActivityRepository that the controller will use
     */
    public ActivityController(ActivityRepository repo) {
        this.repo = repo;
    }

    /**
     * Check if a String is Null or Empty
     * @param s - String to be checked
     * @return true if the String is null or is empty
     */
    private static boolean isNullOrEmpty(String s) {
        return s == null || s.isEmpty();
    }

    /**
     * Check if all the attributes of the activity are neither null nor empty
     * @param activity - Activity to be checked
     * @return true if any of the attributes is null or empty
     */
    private static boolean invalidActivity(Activity activity){
        return  isNullOrEmpty(activity.title) || isNullOrEmpty(activity.consumption)
                || isNullOrEmpty(activity.imagePath) || isNullOrEmpty(activity.source);
    }

    /**
     * Check if the id is existent in the repository
     * @param id - long that will be checked
     * @return true if id < 0 or the id is not in the repository
     */
    private boolean invalidId(long id){return id < 0 || !repo.existsById(id);}

    /**
     * Get all the activities from the repository
     * @return a List containing all the activities from the repository
     */
    @GetMapping(path = { "", "/" })
    public List<Activity> getAllActivities() {
        return repo.findAll();
    }

    /**
     * Get one activity from the repository by its id
     * @return an Activity from the repository that has the same id as the one in the URL
     */
    @GetMapping("/{id}")
    public ResponseEntity<Activity> getActivityById(@PathVariable("id") long id) {
        if (invalidId(id)){
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(repo.findById(id).get());
    }

    /**
     * Add a new activity to the repository
     * @param activity - Activity to be added
     * @return the response with the new activity if added successfully or return bad request if not
     */
    @PostMapping(path = { "", "/" })
    public ResponseEntity<Activity> addActivity(@RequestBody Activity activity) {
        if (invalidActivity(activity)) {
            return ResponseEntity.badRequest().build();
        }

        Activity saved = repo.save(activity);
        return ResponseEntity.ok(saved);
    }

    /**
     * Delete the activity with the given id from the repository
     * @param id - long representing the id of the activity to be removed
     * @return the response with the id of the deleted activity or empty response if there was no activity with the id
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Long> removeActivityById(@PathVariable("id") long id) {
        //Get the activity with the id or null if it does not exist
        Activity activity = repo.findById(id).orElse(null);
        if (activity != null) {
            //Get the id and delete the activity
            long activityId = activity.id;
            repo.delete(activity);

            return ResponseEntity.ok(activityId);
        }
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    /**
     * Update the activity with the given id with the fields of the activity given in the body
     * @param id - long representing the id of the activity that will be modified
     * @param activityDetails - Activity containing the new information
     * @return the updated activity if updated successfully or bad request otherwise
     */
    @PutMapping(path = {"/{id}"})
    public ResponseEntity<Activity> updateActivityById(@PathVariable("id") long id,
                                                       @RequestBody Activity activityDetails) {

        if(invalidId(id) || invalidActivity(activityDetails)){
            return ResponseEntity.badRequest().build();
        }

        Activity activity = repo.findById(id).get();

        //Update the activity with the new attributes given in the request
        activity.title = activityDetails.title;
        activity.consumption = activityDetails.consumption;
        activity.imagePath = activityDetails.imagePath;
        activity.source = activityDetails.source;

        //Save the attribute to the repo
        Activity saved = repo.save(activity);
        return ResponseEntity.ok(saved);
    }

}
