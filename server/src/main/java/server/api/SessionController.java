package server.api;

import commons.GameSession;
import commons.Player;
import commons.Question;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import server.database.SessionRepository;
import server.service.QuestionGenerator;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.stream.Collectors;

import static server.Config.isInvalid;
import static server.Config.isNullOrEmpty;

@RestController
@RequestMapping("api/sessions")
public class SessionController {

    private final SessionRepository repo;
    private final Random random;
    private final ActivityController activityCtrl;

    public SessionController(Random random, SessionRepository repo, String controllerConfig,
                             ActivityController activityCtrl) {
        this.random = random;
        this.repo = repo;
        this.activityCtrl = activityCtrl;
        if (!controllerConfig.equals("test")) resetDatabase(controllerConfig.equals("all"));
    }

    /**
     * Update the current question of a session
     * @param session The session to update the question of
     * @return The session with the updated question
     */
    public GameSession updateQuestion(GameSession session) {
        session.difficultyFactor = session.questionCounter/4 + 1;
        session.questionCounter++;
        Pair<Question, List<Integer>> res = QuestionGenerator.generateQuestion(session.difficultyFactor, activityCtrl);
        session.currentQuestion = res.getKey();
        session.expectedAnswers.clear();
        session.expectedAnswers.addAll(res.getValue());
        return updateSession(session);
    }

    /**
     * End the specified session
     * @param session The session to end
     * @return The ended session
     */
    public GameSession endSession(GameSession session) {
        session.playersReady = 0;
        if (session.sessionType == GameSession.SessionType.SINGLEPLAYER) {
            Player p = session.getPlayers().get(0);
            p.bestSingleScore = Math.max(p.bestSingleScore, p.currentPoints);
            updateSession(session);
            System.out.println("removing session");
            return removeSession(session.id).getBody();
        } else {
            for (Player p : session.players) {
                p.bestMultiScore = Math.max(p.bestMultiScore, p.currentPoints);
            }
            session.setSessionStatus(GameSession.SessionStatus.PAUSED);
            Thread t = new Thread(() -> {
                try {
                    Thread.sleep(5000L);
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                }
                session.setSessionStatus(GameSession.SessionStatus.PLAY_AGAIN);
                repo.save(session);
            });
            t.start();
            return updateSession(session);
        }
    }

    /**
     * Updates the question of a game session
     */
    public GameSession advanceRounds(GameSession session) {
        if (session.sessionStatus == GameSession.SessionStatus.PLAY_AGAIN) {
            session.resetQuestionCounter();
            return repo.save(session);
        } else if (session.questionCounter == GameSession.GAME_ROUNDS) {
            return endSession(session);
        } else if (session.questionCounter == 0) {
            session.playersReady = 0;
            return updateQuestion(session);
        } else {
            System.out.println("Server paused session");
            session.setSessionStatus(GameSession.SessionStatus.PAUSED);
            updateTimeJokers(session.id, 0);
            return updateQuestion(session);
        }
    }

    /**
     * Resets game sessions from previous server runs. Deletes all sessions besides the waiting area
     * and removes all player connections along with them
     *
     * @param resetPersistentData database reset configuration
     */
    public void resetDatabase(boolean resetPersistentData) {
        try (Connection CONN = DriverManager.getConnection("jdbc:h2:file:./quizzzz", "sa", "")) {
            Statement stmt = CONN.createStatement();
            stmt.executeUpdate("DELETE FROM QUESTION_ANSWER_OPTIONS");
            stmt.executeUpdate("DELETE FROM QUESTION_ACTIVITY_PATH");
            stmt.executeUpdate("DELETE FROM GAME_SESSION_EXPECTED_ANSWERS");
            stmt.executeUpdate("DELETE FROM GAME_SESSION_PLAYERS");
            stmt.executeUpdate("DELETE FROM GAME_SESSION_REMOVED_PLAYERS");
            stmt.executeUpdate("DELETE FROM GAME_SESSION WHERE SESSION_TYPE <> 0");
            stmt.executeUpdate("DELETE FROM QUESTION");
            if (resetPersistentData) {
                stmt.executeUpdate("DELETE FROM PLAYER");
                stmt.executeUpdate("DELETE FROM ACTIVITY");
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
    public GameSession updateSession(GameSession session) {
        if (isInvalid(session.id,repo)) return session;
        return this.repo.save(session);
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
        advanceRounds(session);
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
     * Start a new multiplayer game with the players of the waiting area
     * @param waitingArea The waiting area
     */
    public void startNewMultiplayerSession(GameSession waitingArea) {
        // Create new session and transfer all players
        GameSession newSession = new GameSession(GameSession.SessionType.MULTIPLAYER, List.copyOf(waitingArea.players));
        waitingArea.players.clear();
        advanceRounds(newSession);
        repo.save(waitingArea);
        repo.save(newSession);

        // Signal the transfer to the clients
        waitingArea.setSessionStatus(GameSession.SessionStatus.TRANSFERRING);
        repo.save(waitingArea);
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
        if (session.sessionType != GameSession.SessionType.WAITING_AREA &&
                session.playersReady == session.players.size()) {
            advanceRounds(session);
        } else if (session.sessionType == GameSession.SessionType.WAITING_AREA &&
                session.playersReady == session.players.size()) {
            startNewMultiplayerSession(session);
        } else {
            repo.save(session);
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
        if (isInvalid(sessionId,repo)) return ResponseEntity.badRequest().build();
        GameSession session = repo.findById(sessionId).get();
        session.unsetPlayerReady();
        if (session.playersReady == 0) {
            if (session.sessionType == GameSession.SessionType.WAITING_AREA) {
                session.setSessionStatus(GameSession.SessionStatus.WAITING_AREA);
                GameSession newMultiSession = getAvailableSession().getBody();
                if (newMultiSession != null) {
                    newMultiSession.setSessionStatus(GameSession.SessionStatus.ONGOING);
                    repo.save(newMultiSession);
                }
            } else {
                if (session.sessionStatus != GameSession.SessionStatus.PLAY_AGAIN) {
                    session.setSessionStatus(GameSession.SessionStatus.ONGOING);
                }
            }
        }

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
        if (session.sessionStatus == GameSession.SessionStatus.TRANSFERRING &&
                session.sessionType != GameSession.SessionType.WAITING_AREA &&
                session.questionCounter == 0) {
            advanceRounds(session);
        }
        repo.save(session);
        return ResponseEntity.ok(session);
    }
    /**
     * Updates number of timeJokers of game session
     *
     * @param sessionId Id of session to update
     * @param timeJoker  new number of time Jokers
     * @return the number of time jokers
     */
    @GetMapping("/{id}/timeJokers/{timeJoker}")
    public ResponseEntity<Integer> updateTimeJokers(@PathVariable("id") long sessionId,
                                                       @PathVariable("timeJoker") int timeJoker) {
        if (isInvalid(sessionId,repo)) return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        GameSession session = repo.findById(sessionId).get();
        session.setTimeJokers(timeJoker);
        repo.save(session);
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

        if (isInvalid(id,repo)) return ResponseEntity.badRequest().build();
        return ResponseEntity.ok(repo.findById(id).get().players
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

        if (isInvalid(id,repo)) return ResponseEntity.badRequest().build();
        return ResponseEntity.ok(repo.findById(id).get().removedPlayers);
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
        if (session.players.isEmpty()) {
            repo.delete(session);
        } else {
            repo.save(session);
        }

        return ResponseEntity.ok(player);
    }

    /**
     * Sets the questionCounter of a session to zero.
     *
     * @param sessionId The current session.
     * @return          The updated session.
     */
    @GetMapping("/{id}/reset")
    public ResponseEntity<GameSession> resetQuestionCounter(@PathVariable("id") long sessionId) {
        if (isInvalid(sessionId, repo)) return ResponseEntity.badRequest().build();
        GameSession session = repo.findById(sessionId).get();
        session.resetQuestionCounter();
        repo.save(session);
        return ResponseEntity.ok(session);
    }

    /**
     * Check if the given username is active in a session
     * @param username The username to check
     * @return True if the username is used in an active session, otherwise false
     */
    @GetMapping("/checkUsername/{username}")
    public Boolean isUsernameActive(@PathVariable("username") String username) {
        for (GameSession gs : getAllSessions()) {
            Optional<Player> existing = gs
                    .getPlayers()
                    .stream().filter(p -> p.username.equals(username))
                    .findFirst();

            if (existing.isPresent()) return true;
        }
        return false;
    }
}
