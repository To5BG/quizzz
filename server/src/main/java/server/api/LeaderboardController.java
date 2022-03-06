package server.api;

import java.util.List;
import java.util.Random;

import commons.Player;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import server.database.PlayerRepository;

@RestController
@RequestMapping("/api/leaderboard")
public class LeaderboardController {

    private final Random random;
    private final PlayerRepository por;

    public LeaderboardController(Random random, PlayerRepository por) {
        this.random = random;
        this.por = por;
    }

    /**
     * Deliver all Player data in the DB
     * @return a list of all data about past players
     */
    @GetMapping(path = {"", "/"})
    public List<Player> getAllPlayers() {
        return por.findAll();
    }

    /**
     * Query the point of a specific player with his id
     *
     * @param id the random id given to the player
     * @return the point data of this player
     */
    @GetMapping("/{id}")
    public ResponseEntity<Player> getPlayerById(@PathVariable("id") long id) {
        if (id < 0 || !por.existsById(id)) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(por.findById(id).get());
    }

    /**
     * Upload the point of a player to the DB
     * Rookie players are allowed to play the game now!
     * @param player the owner of the point
     * @return return ok when succeeded, badRequest when fail
     */
    @PostMapping(path = {"", "/"})
    public ResponseEntity<Player> addPlayerToRepository(@RequestBody Player player) {

        if (player == null || isNullOrEmpty(player.username)) {
            return ResponseEntity.badRequest().build();
        }

        Player saved = por.save(player);
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

}