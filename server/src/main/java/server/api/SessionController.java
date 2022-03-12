package server.api;

import commons.Answer;
import commons.GameSession;
import commons.Player;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import server.database.SessionRepository;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Comparator;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

import static server.Config.isInvalid;
import static server.Config.isNullOrEmpty;

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
            stmt.executeUpdate("DELETE FROM GAME_SESSION_EXPECTED_ANSWERS");
            stmt.executeUpdate("DELETE FROM GAME_SESSION_PLAYERS");
            stmt.executeUpdate("DELETE FROM GAME_SESSION WHERE SESSION_TYPE <> 0");
            stmt.executeUpdate("DELETE FROM QUESTION");
            if (resetPlayers) {
                stmt.executeUpdate("DELETE FROM PLAYER");
                stmt.executeUpdate("ALTER SEQUENCE HIBERNATE_SEQUENCE RESTART WITH 1");
            }
            if (repo.count() == 0) repo.save(new GameSession(GameSession.SessionType.WAITING_AREA));
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Updates session in database. Called when changes to existing entries are made
     *
     * @param session Session to update
     */
    public void updateSession(GameSession session) {
        if (isInvalid(session.id,repo)) return;
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
                .filter(s -> s.sessionType == GameSession.SessionType.MULTIPLAYER &&
                        s.sessionStatus == GameSession.SessionStatus.STARTED)
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

        if (isInvalid(id,repo)) return ResponseEntity.badRequest().build();
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
            session.currentQuestion = null;
            session.expectedAnswers = null;
            updateSession(session);
            repo.delete(session);
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
    public ResponseEntity<GameSession> setPlayerReady(@PathVariable("id") long sessionId) {
        if (isInvalid(sessionId,repo)) return ResponseEntity.badRequest().build();
        GameSession session = repo.findById(sessionId).get();
        session.setPlayerReady();
        repo.save(session);
        return ResponseEntity.ok(session);
    }

    /**
     * Unsets a player as being ready for a multiplayer game
     *
     * @param sessionId Id of session to update
     * @return new count of ready players
     */
    @GetMapping("/{id}/notready")
    public ResponseEntity<GameSession> unsetPlayerReady(@PathVariable("id") long sessionId) {
        if (isInvalid(sessionId,repo)) return ResponseEntity.badRequest().build();
        GameSession session = repo.findById(sessionId).get();
        session.unsetPlayerReady();
        repo.save(session);
        return ResponseEntity.ok(session);
    }


    /**
     * Updates status of game session
     *
     * @param sessionId Id of session to update
     * @param status    new status of game session
     * @return The updated game session
     */
    @PutMapping("/{id}/status")
    public ResponseEntity<GameSession> updateStatus(@PathVariable("id") long sessionId,
                                                    @RequestBody GameSession.SessionStatus status) {
        if (isInvalid(sessionId,repo)) return ResponseEntity.badRequest().build();
        GameSession session = repo.findById(sessionId).get();
        session.setSessionStatus(status);
        repo.save(session);
        return ResponseEntity.ok(session);
    }
    /**
     * Updates number of timeJokers of game session
     *
     * @param sessionId Id of session to update
     * @param timeJoker  new number of time Jokers
     * @return HTTP status OK if successfully changed
     */
    @PutMapping("/{id}/timeJokers/{timeJoker}")
    public ResponseEntity<HttpStatus> updateTimeJokers(@PathVariable("id") long sessionId,
                                                       @PathVariable("timeJoker") int timeJoker) {
        if (isInvalid(sessionId,repo)) return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        GameSession session = repo.findById(sessionId).get();
        session.setTimeJokers(timeJoker);
        repo.save(session);
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    /**
     * Retrieves all players from a game session
     *
     * @param id id of session
     * @return ResponseEntity that contains a list of all players
     */
    @GetMapping("/{id}/players")
    public ResponseEntity<List<Player>> getPlayers(@PathVariable("id") long id) {

        if (isInvalid(id,repo)) return ResponseEntity.badRequest().build();
        return ResponseEntity.ok(repo.findById(id).get().players
                .stream().sorted(Comparator.comparing(Player::getCurrentPoints).reversed())
                .collect(Collectors.toList()));
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

        if (isInvalid(id,repo)) return ResponseEntity.badRequest().build();
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

        if (isInvalid(sessionId, repo)) return ResponseEntity.badRequest().build();
        GameSession session = repo.findById(sessionId).get();

        Player player = session.players.stream().filter(p -> p.id == playerId).findFirst().orElse(null);
        if (player == null) return ResponseEntity.badRequest().build();

        session.removePlayer(player);
        repo.save(session);
        return ResponseEntity.ok(player);
    }

    /**
     * Fetches the player's answer in parsed form.
     *
     * @param   sessionId The current session.
     * @param   playerId The player who answered.
     * @return  The player's answer in answer form.
     */
    @GetMapping("/{id}/players/{playerId}")
    public ResponseEntity<Answer> getPlayerAnswer(@PathVariable("id") long sessionId,
                                                  @PathVariable("playerId") long playerId) {

        if (isInvalid(sessionId, repo)) return ResponseEntity.badRequest().build();
        GameSession session = repo.findById(sessionId).get();

        Player player = session.players.stream().filter(p -> p.id == playerId).findFirst().orElse(null);
        if (player == null) return ResponseEntity.badRequest().build();

        return ResponseEntity.ok(player.parsedAnswer());
    }

    /**
     * Converts the player's answer to a string and stores it with the player.
     *
     * @param id        The current session.
     * @param playerId  The player who answered.
     * @param ans       The player's answer.
     * @return          The player's answer.
     */
    @PostMapping("/{id}/players/{playerId}")
    public ResponseEntity<Answer> setAnswer(@PathVariable("id") long id, @PathVariable long playerId,
                                            @RequestBody Answer ans) {

        if (isInvalid(id, repo)) return ResponseEntity.badRequest().build();
        GameSession session = repo.findById(id).get();

        Player player = session.players.stream().filter(p -> p.id == playerId).findFirst().orElse(null);
        if (player == null) return ResponseEntity.badRequest().build();

        player.setAnswer(ans);
        repo.save(session);
        return ResponseEntity.ok(ans);
    }
}
