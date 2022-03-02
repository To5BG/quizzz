package server.api;

import commons.GameSession;
import commons.Player;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import server.database.SessionRepository;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.Random;

@RestController
@RequestMapping("api/sessions")
public class SessionController {

    private final SessionRepository repo;
    private final Random random;

    public SessionController(Random random, SessionRepository repo, String controllerConfig) {
        this.random = random;
        this.repo = repo;
        if (!controllerConfig.equals("test")) resetDatabase(controllerConfig.equals("all"));
    }

    /**
     * Resets game sessions from previous server runs. Deletes all sessions besides the waiting area
     * and removes all player connections along with them
     *
     * @param resetPlayers True iff the players' table should also be removed
     */
    public void resetDatabase(boolean resetPlayers) {
        try (Connection conn = DriverManager.getConnection("jdbc:h2:file:./quizzzz", "sa", "")) {
            Statement stmt = conn.createStatement();
            stmt.executeUpdate("DELETE FROM QUESTION_ANSWER_OPTIONS");
            stmt.executeUpdate("DELETE FROM QUESTION");
            stmt.executeUpdate("DELETE FROM GAME_SESSION_EXPECTED_ANSWERS");
            stmt.executeUpdate("DELETE FROM GAME_SESSION_PLAYERS");
            stmt.executeUpdate("DELETE FROM GAME_SESSION");
            stmt.executeUpdate("ALTER SEQUENCE HIBERNATE_SEQUENCE RESTART WITH 1");
            repo.save(new GameSession("waiting_area"));
            if (resetPlayers) stmt.executeUpdate("DELETE FROM PLAYER");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void updateSession(GameSession session) {
        if (isInvalid(session.id)) return;
        this.repo.save(session);
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
     * Adds a session to the DB
     *
     * @param session Session to be added
     * @return ResponseEntity that contains the added session
     */
    @PostMapping(path = {"", "/"})
    public ResponseEntity<GameSession> addSession(@RequestBody GameSession session) {

        if (session.players == null) return ResponseEntity.badRequest().build();
        for (Player p : session.players) {
            if (isNullOrEmpty(p.username)) return ResponseEntity.badRequest().build();
        }
        session.updateQuestion();
        GameSession saved = repo.save(session);
        return ResponseEntity.ok(saved);
    }

    /**
     * Retrieves an available game session from the DB.
     *
     * @return Available game session
     */
    @GetMapping({"/join"})
    public ResponseEntity<GameSession> getAvailableSession() {
        var session = repo.findAll().stream()
                .filter(s -> s.sessionType.equals("multiplayer") && s.sessionStatus.equals("started"))
                .findFirst();
        if (session.isEmpty()) return ResponseEntity.ok(null);
        else return ResponseEntity.ok(session.get());
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
     * Remove a session from the DB.
     *
     * @param id id of session to be removed
     * @return ResponseEntity that contains the removed session
     */
    @DeleteMapping({"/{id}"})
    public ResponseEntity<GameSession> removeSession(@PathVariable("id") long id) {

        GameSession session = repo.findById(id).orElse(null);
        if (session != null) {
            repo.delete(session);
            session.currentQuestion = null;
        }
        return ResponseEntity.ok(session);
    }

    /**
     * Sets an additional player as ready for a multiplayer game
     *
     * @param sessionId Id of session to update
     * @return new count of ready players
     */
    @GetMapping("/{id}/ready")
    public ResponseEntity<Integer> setPlayerReady(@PathVariable("id") long sessionId) {
        if (isInvalid(sessionId)) return ResponseEntity.badRequest().build();
        GameSession session = repo.findById(sessionId).get();
        session.setPlayerReady();
        repo.save(session);
        return ResponseEntity.ok(session.playersReady);
    }

    /**
     * Unsets a player as being ready for a multiplayer game
     *
     * @param sessionId Id of session to update
     * @return new count of ready players
     */
    @GetMapping("/{id}/notready")
    public ResponseEntity<Integer> unsetPlayerReady(@PathVariable("id") long sessionId) {
        if (isInvalid(sessionId)) return ResponseEntity.badRequest().build();
        GameSession session = repo.findById(sessionId).get();
        session.unsetPlayerReady();
        repo.save(session);
        return ResponseEntity.ok(session.playersReady);
    }


    @PutMapping("/{id}/status")
    public ResponseEntity<GameSession> updateStatus(@PathVariable("id") long sessionId, @RequestBody String status) {
        if (isInvalid(sessionId)) return ResponseEntity.badRequest().build();
        GameSession session = repo.findById(sessionId).get();
        session.updateStatus(status);
        repo.save(session);
        return ResponseEntity.ok(session);
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
