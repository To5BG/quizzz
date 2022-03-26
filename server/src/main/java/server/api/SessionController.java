package server.api;

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
import java.util.Optional;
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
     * Update the current question of a session
     * @param session The session to update the question of
     */
    public void updateQuestion(GameSession session) {
        session.difficultyFactor = session.questionCounter / 4 + 1;
        session.questionCounter++;
        Pair<Question, List<Long>> res = QuestionGenerator.generateQuestion(session.difficultyFactor, activityCtrl);
        session.currentQuestion = res.getKey();
        session.expectedAnswers.clear();
        session.expectedAnswers.addAll(res.getValue());
        updateSession(session);
    }

    /**
     * End the specified session
     * @param session The session to end
     */
    public void endSession(GameSession session) {
        session.playersReady.set(0);
        if (session.sessionType == GameSession.SessionType.SINGLEPLAYER) {
            Player p = session.getPlayers().get(0);
            p.bestSingleScore = Math.max(p.bestSingleScore, p.currentPoints);
            repo.save(p);
            System.out.println("removing session");
            removeSession(session.id);
        } else {
            for (Player p : session.players) {
                p.bestMultiScore = Math.max(p.bestMultiScore, p.currentPoints);
                repo.save(p);
            }
            session.setSessionStatus(GameSession.SessionStatus.PAUSED);
            Thread t = new Thread(() -> {
                try {
                    Thread.sleep(1000L);
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                }
                session.setSessionStatus(GameSession.SessionStatus.PLAY_AGAIN);
                updateSession(session);
            });
            t.start();
            updateSession(session);
        }
    }

    /**
     * Updates the question of a game session
     */
    public void advanceRounds(GameSession session) {
        updateTimeJokers(session.id, 0);
        if (session.sessionStatus == GameSession.SessionStatus.PLAY_AGAIN) {
            session.resetQuestionCounter();
            for (Player p : session.players) {
                p.currentPoints = 0;
            }
            updateSession(session);
        } else if (session.questionCounter == GameSession.GAME_ROUNDS) {
            endSession(session);
        } else if (session.questionCounter == 0) {
            session.playersReady.set(0);
            updateQuestion(session);
        } else {
            System.out.println("Server paused session");
            session.setSessionStatus(GameSession.SessionStatus.PAUSED);
            updateQuestion(session);
        }
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
            repo.save(p);
        }
        advanceRounds(session);
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
     * Start a new multiplayer game with the players of the waiting area
     * @param waitingArea The waiting area
     */
    public void startNewMultiplayerSession(GameSession waitingArea) {
        // Create new session with all waiting players
        GameSession newSession = new GameSession(GameSession.SessionType.MULTIPLAYER);
        newSession.players.addAll(waitingArea.players);
        addSession(newSession);

        // Signal the transfer to the clients and remove them from waiting area
        waitingArea.players.clear();
        waitingArea.setSessionStatus(GameSession.SessionStatus.TRANSFERRING);
        updateSession(waitingArea);
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
                session.playersReady.get() == session.players.size()) {
            advanceRounds(session);
        } else if (session.sessionType == GameSession.SessionType.WAITING_AREA &&
                session.playersReady.get() == session.players.size()) {
            startNewMultiplayerSession(session);
        } else {
            updateSession(session);
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
        if (session.playersReady.get() == 0) {
            if (session.sessionType == GameSession.SessionType.WAITING_AREA) {
                session.setSessionStatus(GameSession.SessionStatus.WAITING_AREA);
                GameSession newMultiSession = getAvailableSession().getBody();
                if (newMultiSession != null) {
                    newMultiSession.setSessionStatus(GameSession.SessionStatus.ONGOING);
                    updateSession(newMultiSession);
                }
            } else {
                if (session.sessionStatus != GameSession.SessionStatus.PLAY_AGAIN) {
                    session.setSessionStatus(GameSession.SessionStatus.ONGOING);
                }
            }
        }

        updateSession(session);
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
        if (session.sessionStatus == GameSession.SessionStatus.TRANSFERRING &&
                session.sessionType != GameSession.SessionType.WAITING_AREA &&
                session.questionCounter == 0) {
            advanceRounds(session);
        }
        updateSession(session);
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
        if (session.players.isEmpty()) {
            removeSession(session.id);
        } else {
            updateSession(session);
        }
        return ResponseEntity.ok(player);
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
