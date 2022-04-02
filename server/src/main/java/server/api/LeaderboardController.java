package server.api;

import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import commons.Player;

import org.springframework.data.util.Pair;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import org.springframework.web.context.request.async.DeferredResult;
import server.database.PlayerRepository;

import static server.Config.isNullOrEmpty;

@SuppressWarnings("OptionalGetWithoutIsPresent")
@RestController
@RequestMapping("/api/leaderboard")
public class LeaderboardController {

    private final PlayerRepository repo;
    private boolean multiChangesToCommit = false;

    /**
     * @param por the repository of players
     */
    public LeaderboardController(PlayerRepository por) {
        this.repo = por;
    }

    /**
     * An API to return all players in the DB
     *
     * @return a list of all players in the DB
     */
    @GetMapping(path = {"/"})
    public ResponseEntity<List<Player>> getAllPlayers() {
        var list = repo.findAll();
        return ResponseEntity.ok(list);
    }

    /**
     * Deliver all Player data in the DB
     * sorted by best single mode score, filtered all players with 0 score
     *
     * @return a list of all data about players in single mode
     */
    @GetMapping(path = {"/single"})
    public ResponseEntity<List<Player>> getPlayerSingleScores() {
        var list = repo.findByOrderByBestSingleScoreDesc().stream()
                .filter(player -> player.getBestSingleScore() != 0)
                .collect(Collectors.toList());
        return ResponseEntity.ok(list);
    }

    /**
     * Deliver all Player data in the DB
     * sorted by best multi mode score, filtered all players with 0 score
     *
     * @return a list of all data about players in multi mode
     */
    @GetMapping(path = {"/multi"})
    public ResponseEntity<List<Player>> getPlayerMultiScores() {
        var list = repo.findByOrderByBestMultiScoreDesc().stream()
                .filter(player -> player.getBestMultiScore() != 0)
                .collect(Collectors.toList());
        return ResponseEntity.ok(list);
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
     * Get a player object with the specified username
     *
     * @param username The username of the player to check
     * @return The player object if the username is found, otherwise null
     */
    @GetMapping("/getByUsername/{username}")
    public ResponseEntity<Player> getPlayerByUsername(@PathVariable("username") String username) {
        Optional<Player> result = repo.findAll().stream().filter(p -> p.username.equals(username)).findFirst();
        return ResponseEntity.ok(result.orElse(null));
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
    @PutMapping("/{id}/singlescore")
    public ResponseEntity<Player> updateCurrentSinglePoints(@PathVariable("id") long playerId,
                                                            @RequestBody int points) {
        if (playerId < 0 || !repo.existsById(playerId)) return ResponseEntity.badRequest().build();
        Player updatedPlayer = repo.findById(playerId).get();
        updatedPlayer.currentPoints += points;
        repo.save(updatedPlayer);
        return ResponseEntity.ok(updatedPlayer);
    }

    /**
     * Updates current points of a player entry
     *
     * @param playerId Id of player
     * @param points   Point count to be updated with
     * @return Updated player entity
     */
    @PutMapping("/{id}/multiscore")
    public ResponseEntity<Player> updateCurrentMultiPoints(@PathVariable("id") long playerId,
                                                           @RequestBody int points) {
        if (playerId < 0 || !repo.existsById(playerId)) return ResponseEntity.badRequest().build();
        Player updatedPlayer = repo.findById(playerId).get();
        updatedPlayer.currentPoints += points;
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
    @PutMapping("/{id}/bestsinglescore")
    public ResponseEntity<Player> updateBestSingleScore(@PathVariable("id") long playerId,
                                                        @RequestBody int points) {
        if (playerId < 0 || !repo.existsById(playerId)) return ResponseEntity.badRequest().build();
        Player updatedPlayer = repo.findById(playerId).get();
        if (points > updatedPlayer.getBestSingleScore()) {
            updatedPlayer.setBestSingleScore(points);
            repo.save(updatedPlayer);
            listeners.forEach((k, l) -> l.accept(Pair.of("single", this.getPlayerSingleScores().getBody())));
        }
        return ResponseEntity.ok(updatedPlayer);
    }

    /**
     * Updates best points of a player entry
     *
     * @param playerId Id of player
     * @param points   Point count to be updated with
     * @return Updated player entity
     */
    @PutMapping("/{id}/bestmultiscore")
    public ResponseEntity<Player> updateBestMultiScore(@PathVariable("id") long playerId,
                                                       @RequestBody int points) {
        if (playerId < 0 || !repo.existsById(playerId)) return ResponseEntity.badRequest().build();
        Player updatedPlayer = repo.findById(playerId).get();
        updatedPlayer.setBestMultiScore(points);
        repo.save(updatedPlayer);
        if (points > updatedPlayer.getBestMultiScore()) {
            updatedPlayer.setBestMultiScore(points);
            repo.save(updatedPlayer);
            multiChangesToCommit = true;
        }
        return ResponseEntity.ok(updatedPlayer);
    }

    Map<Object, Consumer<Pair<String, List<Player>>>> listeners = new HashMap<>();

    /**
     * Register client listener for leaderboard updates
     *
     * @return DeferredResult that contains updates on leaderboard, if any
     */
    @GetMapping("/updates")
    public DeferredResult<ResponseEntity<List<Player>>> getLeaderboardUpdates() {
        var emptyContent = ResponseEntity.status(HttpStatus.NO_CONTENT).build();
        var res = new DeferredResult<ResponseEntity<List<Player>>>(5000L, emptyContent);

        var k = new Object();
        listeners.put(k, p -> {
            HttpHeaders headers = new HttpHeaders();
            headers.set("X-gamemodeType", p.getFirst());
            res.setResult(new ResponseEntity<List<Player>>(p.getSecond(), headers, HttpStatus.OK));
        });
        res.onCompletion(() -> listeners.remove(k));
        return res;
    }

    /**
     * Informs clients of multiplayer best score changes once all transactions are committed
     */
    public void commitMultiplayerUpdates() {
        if (!multiChangesToCommit) return;
        listeners.forEach((k, l) -> l.accept(Pair.of("multi", this.getPlayerMultiScores().getBody())));
        multiChangesToCommit = false;
    }

}