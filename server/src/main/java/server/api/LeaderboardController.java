package server.api;

import java.util.List;
import java.util.Random;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import commons.Point;
import server.database.PointRepository;

@RestController
@RequestMapping("/api/point")
public class LeaderboardController {

    private final Random random;
    private final PointRepository por;

    public LeaderboardController(Random random, PointRepository por) {
        this.random = random;
        this.por = por;
    }

    /**
     * Deliver all Point data in the DB
     *
     * @return a list of all data about point
     */
    @GetMapping(path = {"", "/"})
    public List<Point> getAll() {
        return por.findAll();
    }

    /**
     * Query the point of a specific player with this id
     *
     * @param id the random id given to the player
     * @return the point data of this player
     */
    @GetMapping("/{id}")
    public ResponseEntity<Point> getById(@PathVariable("id") long id) {
        if (id < 0 || !por.existsById(id)) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(por.findById(id).get());
    }

    /**
     * Upload the point of a player to the DB
     *
     * @param point the point of a player
     * @return return ok when succeeded, badRequest when fail
     */
    @PostMapping(path = {"", "/"})
    public ResponseEntity<Point> add(@RequestBody Point point) {

        if (point.player == null || isNullOrEmpty(point.player.username) || point.point == 0) {
            return ResponseEntity.badRequest().build();
        }

        Point saved = por.save(point);
        return ResponseEntity.ok(saved);
    }

    /**
     * judge whether the String is empty or null
     *
     * @param s a string to be judged
     * @return a boolean value which represents whether the string is empty or null
     */
    private static boolean isNullOrEmpty(String s) {
        return s == null || s.isEmpty();
    }

    /**
     * give a random number
     *
     * @return a random number
     */
    @GetMapping("rnd")
    public ResponseEntity<Point> getRandom() {
        var idx = random.nextInt((int) por.count());
        return ResponseEntity.ok(por.getById((long) idx));
    }
}