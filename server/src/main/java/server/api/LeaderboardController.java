package server.api;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import commons.Player;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import server.database.PlayerRepository;

import static server.Config.isNullOrEmpty;

@SuppressWarnings("OptionalGetWithoutIsPresent")
@RestController
@RequestMapping("/api/leaderboard")
public class LeaderboardController {

    private final PlayerRepository repo;

    public LeaderboardController(PlayerRepository por) {
        this.repo = por;
    }

    /**
     * Deliver all Player data in the DB
     *
     * @return a list of all data about past players
     */
    @GetMapping(path = { "/single"})
    public ResponseEntity<List<Player>> getPlayerSingleScores() {
        return ResponseEntity.ok(repo.findAll()
                .stream().sorted(Comparator.comparing(Player::getBestSingleScore).reversed())
                .collect(Collectors.toList()));
    }

    @GetMapping(path = {"/multi"})
    public ResponseEntity<List<Player>> getPlayerMultiScore() {
        return ResponseEntity.ok(repo.findAll()
                .stream().sorted(Comparator.comparing(Player::getBestMultiScore).reversed())
                .collect(Collectors.toList()));
    }

    /**
     * Query the point of a specific player with his id
     *
     * @param id the random id given to the player
     * @return the point data of this player
     */
    @GetMapping("/{id}")
    public ResponseEntity<Player> getPlayerById(@PathVariable("id") long id) {
        if (id < 0 || !repo.existsById(id)) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(repo.findById(id).get());
    }

    /**
     * Adds a player entry to the database with no associated game session/forcibly
     *
     * @param player Player to be added
     * @return ResponseEntity that contains added player entry
     */
    @PostMapping(path = {"", "/"})
    public ResponseEntity<Player> addPlayerForcibly(@RequestBody Player player) {

        if (player == null || isNullOrEmpty(player.username)) {
            return ResponseEntity.badRequest().build();
        }

        Player saved = repo.save(player);
        return ResponseEntity.ok(saved);
    }

    /**
     * Updates current points of a player entry
     *
     * @param playerId Id of player
     * @param points   Point count to be updated with
     * @return Updated player entity
     */
    @PutMapping("/{id}/score")
    public ResponseEntity<Player> updateCurrentPoints(@PathVariable("id") long playerId,
                                                      @RequestBody int points) {
        if (playerId < 0 || !repo.existsById(playerId)) return ResponseEntity.badRequest().build();
        Player updatedPlayer = repo.findById(playerId).get();
        updatedPlayer.setCurrentPoints(points);
        repo.save(updatedPlayer);
        return ResponseEntity.ok(updatedPlayer);
    }

    /**
     * Updates best points of a player entry
     *
     * @param playerId Id of player
     * @param points   Point count to be updated with
     * @return Updated player entity
     */
    @PutMapping("/{id}/bestscore")
    public ResponseEntity<Player> updateBestSingleScore(@PathVariable("id") long playerId,
                                                  @RequestBody int points) {
        if (playerId < 0 || !repo.existsById(playerId)) return ResponseEntity.badRequest().build();
        Player updatedPlayer = repo.findById(playerId).get();
        updatedPlayer.setBestSingleScore(points);

        /* assumption that best score is updated only at the end of a game */
        // updatedPlayer.setCurrentPoints(0);

        repo.save(updatedPlayer);
        return ResponseEntity.ok(updatedPlayer);
    }

}