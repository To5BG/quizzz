package server.api;

import commons.GameSession;
import commons.Player;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import server.database.SessionRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@RestController
@RequestMapping("api/sessions")
public class SessionController {

    private final SessionRepository repo;
    private final Random random;

    public SessionController(Random random, SessionRepository repo) {
        this.random = random;
        this.repo = repo;
    }

    /**
     * Retrieve all sessions from the DB.
     *
     * @return All game sessions
     */
    @GetMapping(path = {"", "/"})
    public List<GameSession> getAllSessions() {
        return repo.findAll();
    }

    /**
     * Retrieves an available game session from the DB.
     *
     * @return Available game session
     */
    @GetMapping({"/join"})
    public ResponseEntity<GameSession> getAvailableSession() {
        var sessions = repo.findAll();
        var sessionToJoin = (sessions.isEmpty())
                ? addSession(new GameSession(new ArrayList<>())).getBody() : sessions.get(0);
        return ResponseEntity.ok(sessionToJoin);
    }

    /**
     * Retrieves a session by the given id
     *
     * @param id id of session
     * @return ResponseEntity that contains the retrieved session
     */
    @GetMapping("/{id}")
    public ResponseEntity<GameSession> getSessionById(@PathVariable("id") long id) {

        if (isInvalid(id)) return ResponseEntity.badRequest().build();
        return ResponseEntity.ok(repo.findById(id).get());
    }

    /**
     * Adds a session to the DB
     *
     * @param session Session to be added
     * @return ResponseEntity that contains the added session
     */
    @PutMapping(path = {"", "/"})
    public ResponseEntity<GameSession> addSession(@RequestBody GameSession session) {

        if (session.players == null) return ResponseEntity.badRequest().build();
        for (Player p : session.players) {
            if (isNullOrEmpty(p.username)) return ResponseEntity.badRequest().build();
        }
        GameSession saved = repo.save(session);
        return ResponseEntity.ok(saved);
    }

    /**
     * Remove a session from the DB.
     *
     * @param id id of session to be removed
     * @return ResponseEntity that contains the removed session
     */
    @DeleteMapping({"/{id}"})
    public ResponseEntity<GameSession> removeSession(@PathVariable("id") long id) {

        GameSession session = repo.findById(id).orElse(null);
        if (session != null) {
            long sessionId = session.id;
            repo.delete(session);
            return ResponseEntity.ok(session);
        }
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    /**
     * Retrieves all players from a game session
     *
     * @param id id of session
     * @return ResponseEntity that contains a list of all players
     */
    @GetMapping("/{id}/players")
    public ResponseEntity<List<Player>> getPlayers(@PathVariable("id") long id) {

        if (isInvalid(id)) return ResponseEntity.badRequest().build();
        return ResponseEntity.ok(repo.findById(id).get().players);
    }

    /**
     * Adds a player to a game session
     *
     * @param id     id of game session
     * @param player Player object to be added
     * @return ResponseEntity that contains the added player
     */
    @PostMapping("/{id}/players")
    public ResponseEntity<Player> addPlayer(@PathVariable("id") long id, @RequestBody Player player) {

        if (isInvalid(id)) return ResponseEntity.badRequest().build();
        GameSession session = repo.findById(id).get();

        session.addPlayer(player);
        repo.save(session);
        return ResponseEntity.ok(player);
    }

    /**
     * Remove a player from a game session
     *
     * @param sessionId id of game session
     * @param playerId  id of player
     * @return ResponseEntity that contains the removed player
     */
    @DeleteMapping("/{id}/players/{playerId}")
    public ResponseEntity<Player> removePlayer(@PathVariable("id") long sessionId,
                                               @PathVariable("playerId") long playerId) {

        if (isInvalid(sessionId)) return ResponseEntity.badRequest().build();
        GameSession session = repo.findById(sessionId).get();

        Player player = session.players.stream().filter(p -> p.id == playerId).findFirst().orElse(null);
        if (player == null) return ResponseEntity.badRequest().build();

        session.removePlayer(player);
        repo.save(session);
        return ResponseEntity.ok(player);
    }

    /**
     * Checks whether a string is empty or null.
     *
     * @param s String to be checked
     * @return True iff the object is either null or an empty string
     */
    private static boolean isNullOrEmpty(String s) {
        return s == null || s.isEmpty();
    }

    /**
     * Checks if the provided id is invalid for the game session repo.
     *
     * @param id id to be checked
     * @return True iff the id is a negative integer or no entry has the provided id
     */
    private boolean isInvalid(long id) {
        return id < 0 || !repo.existsById(id);
    }
}
