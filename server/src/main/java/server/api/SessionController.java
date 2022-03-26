package server.api;

import commons.Answer;
import commons.GameSession;
import commons.Player;
import commons.Question;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import server.database.PlayerRepository;
import server.service.QuestionGenerator;
import server.service.SessionManager;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Comparator;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

import static server.Config.isNullOrEmpty;

@RestController
@RequestMapping("api/sessions")
public class SessionController {

    private final PlayerRepository repo;
    private final SessionManager sm;
    private final Random random;
    private final ActivityController activityCtrl;

    public SessionController(Random random, PlayerRepository repo, String controllerConfig, SessionManager sm,
                             ActivityController activityCtrl) {
        this.random = random;
        this.repo = repo;
        this.sm = sm;
        this.activityCtrl = activityCtrl;
        if (!controllerConfig.equals("test")) sm.save(new GameSession(GameSession.SessionType.WAITING_AREA));
        if (controllerConfig.equals("all")) resetDatabase();
    }

    /**
     * Updates the question of a game session
     */
    public void updateQuestion(GameSession session) {
        session.difficultyFactor = session.questionCounter / 4 + 1;
        session.questionCounter++;
        Pair<Question, List<Integer>> res = QuestionGenerator.generateQuestion(session.difficultyFactor, activityCtrl);
        session.currentQuestion = res.getKey();
        System.out.println("Question updated to:");
        System.out.println(session.currentQuestion);
        session.expectedAnswers.clear();
        session.expectedAnswers.addAll(res.getValue());
    }

    /**
     * Resets the database, wiping all previous persistent data on it
     *
     */
    public void resetDatabase() {
        try (Connection CONN = DriverManager.getConnection("jdbc:h2:file:./quizzzz", "sa", "")) {
            Statement stmt = CONN.createStatement();
            stmt.executeUpdate("DELETE FROM PLAYER");
            stmt.executeUpdate("DELETE FROM ACTIVITY");
            stmt.executeUpdate("ALTER SEQUENCE HIBERNATE_SEQUENCE RESTART WITH 1");
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
        if (sm.isValid(session.id)) sm.save(session);
    }

    /**
     * Retrieve all sessions from the DB.
     *
     * @return All game sessions
     */
    @GetMapping(path = {"", "/"})
    public List<GameSession> getAllSessions() {
        return sm.getValues();
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
        updateQuestion(session);
        GameSession saved = sm.save(session);
        return ResponseEntity.ok(saved);
    }

    /**
     * Retrieves an available game session from the DB.
     *
     * @return Available game session
     */
    @GetMapping({"/join"})
    public ResponseEntity<GameSession> getAvailableSession() {
        var session = sm.getValues().stream()
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
        GameSession res = sm.getById(id);
        return (res == null) ? ResponseEntity.badRequest().build() : ResponseEntity.ok(res);
    }

    /**
     * Remove a session from the DB.
     *
     * @param id id of session to be removed
     * @return ResponseEntity that contains the removed session
     */
    @DeleteMapping({"/{id}"})
    public ResponseEntity<GameSession> removeSession(@PathVariable("id") long id) {
        return ResponseEntity.ok(sm.delete(id));
    }

    /**
     * Sets an additional player as ready for a multiplayer game
     *
     * @param sessionId Id of session to update
     * @return new count of ready players
     */
    @GetMapping("/{id}/ready")
    public ResponseEntity<GameSession> setPlayerReady(@PathVariable("id") long sessionId) {
        if (!sm.isValid(sessionId)) return ResponseEntity.badRequest().build();
        GameSession session = sm.getById(sessionId);
        session.setPlayerReady();
        if (session.sessionType != GameSession.SessionType.WAITING_AREA &&
                session.playersReady == session.players.size()) {
            updateQuestion(session);
        }
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
        if (!sm.isValid(sessionId)) return ResponseEntity.badRequest().build();
        GameSession session = sm.getById(sessionId);
        session.unsetPlayerReady();
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
        if (!sm.isValid(sessionId)) return ResponseEntity.badRequest().build();
        GameSession session = sm.getById(sessionId);
        session.setSessionStatus(status);
        return ResponseEntity.ok(session);
    }

    /**
     * Updates number of timeJokers of game session
     *
     * @param sessionId Id of session to update
     * @param timeJoker new number of time Jokers
     * @return the number of time jokers
     */
    @GetMapping("/{id}/timeJokers/{timeJoker}")
    public ResponseEntity<Integer> updateTimeJokers(@PathVariable("id") long sessionId,
                                                    @PathVariable("timeJoker") int timeJoker) {
        if (!sm.isValid(sessionId)) return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        GameSession session = sm.getById(sessionId);
        session.setTimeJokers(timeJoker);
        return ResponseEntity.ok(session.timeJokers);
    }

    /**
     * Retrieves all players from a game session
     *
     * @param id id of session
     * @return ResponseEntity that contains a list of all players
     */
    @GetMapping("/{id}/players")
    public ResponseEntity<List<Player>> getPlayers(@PathVariable("id") long id) {
        if (!sm.isValid(id)) return ResponseEntity.badRequest().build();
        return ResponseEntity.ok(sm.getById(id).players
                .stream().sorted(Comparator.comparing(Player::getCurrentPoints).reversed())
                .collect(Collectors.toList()));
    }

    /**
     * Retrieves all the removed players from the game session
     *
     * @param id id of session
     * @return ResponseEntity that contains the list of all removed players
     */
    @GetMapping("/{id}/removedPlayers")
    public ResponseEntity<List<Player>> getRemovedPlayers(@PathVariable("id") long id) {
        if (!sm.isValid(id)) return ResponseEntity.badRequest().build();
        return ResponseEntity.ok(sm.getById(id).removedPlayers);
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
        if (!sm.isValid(id)) return ResponseEntity.badRequest().build();
        GameSession session = sm.getById(id);

        session.addPlayer(player);
        repo.save(player);
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
        if (!sm.isValid(sessionId)) return ResponseEntity.badRequest().build();
        GameSession session = sm.getById(sessionId);

        Player player = session.players.stream().filter(p -> p.id == playerId).findFirst().orElse(null);
        if (player == null) return ResponseEntity.badRequest().build();

        session.removePlayer(player);
        return ResponseEntity.ok(player);
    }

    /**
     * Fetches the player's answer in parsed form.
     *
     * @param sessionId The current session.
     * @param playerId  The player who answered.
     * @return The player's answer in answer form.
     */
    @GetMapping("/{id}/players/{playerId}")
    public ResponseEntity<Answer> getPlayerAnswer(@PathVariable("id") long sessionId,
                                                  @PathVariable("playerId") long playerId) {
        if (!sm.isValid(sessionId)) return ResponseEntity.badRequest().build();
        GameSession session = sm.getById(sessionId);

        Player player = session.players.stream().filter(p -> p.id == playerId).findFirst().orElse(null);
        if (player == null) return ResponseEntity.badRequest().build();

        return ResponseEntity.ok(player.parsedAnswer());
    }

    /**
     * Converts the player's answer to a string and stores it with the player.
     *
     * @param sessionId The current session.
     * @param playerId  The player who answered.
     * @param ans       The player's answer.
     * @return The player's answer.
     */
    @PostMapping("/{id}/players/{playerId}")
    public ResponseEntity<Answer> setAnswer(@PathVariable("id") long sessionId, @PathVariable long playerId,
                                            @RequestBody Answer ans) {
        if (!sm.isValid(sessionId)) return ResponseEntity.badRequest().build();
        GameSession session = sm.getById(sessionId);

        Player player = session.players.stream().filter(p -> p.id == playerId).findFirst().orElse(null);
        if (player == null) return ResponseEntity.badRequest().build();

        player.setAnswer(ans);
        return ResponseEntity.ok(ans);
    }

    /**
     * Sets the questionCounter of a session to zero.
     *
     * @param sessionId The current session.
     * @return The updated session.
     */
    @GetMapping("/{id}/reset")
    public ResponseEntity<GameSession> resetQuestionCounter(@PathVariable("id") long sessionId) {
        if (!sm.isValid(sessionId)) return ResponseEntity.badRequest().build();
        GameSession session = sm.getById(sessionId);

        session.resetQuestionCounter();
        return ResponseEntity.ok(session);
    }
}
