package client.utils;

import commons.GameSession;
import commons.Joker;
import commons.Player;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.core.GenericType;
import org.glassfish.jersey.client.ClientConfig;

import java.util.List;

import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON;

public class GameSessionUtils {

    public static String serverConnection = "http://localhost:8080/";

    /**
     * Retrieves a game session from the DB.
     *
     * @param sessionId id of the session to retrieve
     * @return Game session with the given id
     */
    public GameSession getSession(long sessionId) {
        return ClientBuilder.newClient(new ClientConfig())
                .target(serverConnection).path("api/sessions/" + sessionId)
                .request(APPLICATION_JSON)
                .accept(APPLICATION_JSON)
                .get(new GenericType<GameSession>() {
                });
    }

    /**
     * Retrieves all available waiting rooms from the DB.
     *
     * @return Available game session
     */
    public List<GameSession> getAvailableSessions() {
        return ClientBuilder.newClient(new ClientConfig())
                .target(serverConnection).path("api/sessions/available")
                .request(APPLICATION_JSON)
                .accept(APPLICATION_JSON)
                .get(new GenericType<List<GameSession>>() {
                });
    }

    /**
     * Retrieves all game sessions from the DB that are still active.
     *
     * @return All active game sessions
     */
    public List<GameSession> getSessions() {
        return ClientBuilder.newClient(new ClientConfig())
                .target(serverConnection).path("api/sessions")
                .request(APPLICATION_JSON)
                .accept(APPLICATION_JSON)
                .get(new GenericType<List<GameSession>>() {
                });
    }

    /**
     * Adds a session to the DB.
     *
     * @param session GameSession object to be added
     * @return The session that has been added
     */
    public GameSession addSession(GameSession session) {
        return ClientBuilder.newClient(new ClientConfig())
                .target(serverConnection).path("api/sessions")
                .request(APPLICATION_JSON)
                .accept(APPLICATION_JSON)
                .post(Entity.entity(session, APPLICATION_JSON), GameSession.class);
    }

    /**
     * Adds a waiting room to the DB.
     *
     * @param session GameSession object to be added
     * @return The session that has been added
     */
    public GameSession addWaitingRoom(GameSession session) {
        return ClientBuilder.newClient(new ClientConfig())
                .target(serverConnection).path("api/sessions/waiting")
                .request(APPLICATION_JSON)
                .accept(APPLICATION_JSON)
                .post(Entity.entity(session, APPLICATION_JSON), GameSession.class);
    }

    /**
     * Removes a session from the DB.
     *
     * @param sessionId Id of session to be removed
     * @return The response from session removal
     */
    public GameSession removeSession(long sessionId) {
        return ClientBuilder.newClient(new ClientConfig())
                .target(serverConnection).path("api/sessions/" + sessionId)
                .request(APPLICATION_JSON)
                .accept(APPLICATION_JSON)
                .delete(GameSession.class);
    }

    /**
     * Updates a session status
     *
     * @param session Session to update
     * @param status  new status to be set
     * @return The updated session
     */
    public GameSession updateStatus(GameSession session, GameSession.SessionStatus status) {
        return ClientBuilder.newClient(new ClientConfig())
                .target(serverConnection).path("api/sessions/" + session.id + "/status")
                .request(APPLICATION_JSON)
                .accept(APPLICATION_JSON)
                .put(Entity.entity(status, APPLICATION_JSON), GameSession.class);
    }

    /**
     * Sets and unsets a player as being ready for a multiplayer game
     *
     * @param sessionId the id of the session
     * @param isReady   True iff a player must be set as ready
     * @return New count of players that are ready
     */
    public GameSession toggleReady(long sessionId, boolean isReady) {
        return ClientBuilder.newClient(new ClientConfig())
                .target(serverConnection).path("api/sessions/" + sessionId + "/" + ((isReady) ? "" : "not") + "ready")
                .request(APPLICATION_JSON)
                .accept(APPLICATION_JSON)
                .get(new GenericType<GameSession>() {
                });
    }

    /**
     * Adds a player to a game session.
     *
     * @param sessionId id of the session to add the player to
     * @param player    Player object to be added
     * @return The player that has been added
     */
    public Player addPlayer(long sessionId, Player player) {
        return ClientBuilder.newClient(new ClientConfig())
                .target(serverConnection).path("api/sessions/" + sessionId + "/players")
                .request(APPLICATION_JSON)
                .accept(APPLICATION_JSON)
                .post(Entity.entity(player, APPLICATION_JSON), Player.class);
    }

    /**
     * Removes a player from a game session.
     *
     * @param sessionId id of the session to remove the player from
     * @param playerId  id of player to be removed
     * @return The response from player removal
     */
    public Player removePlayer(long sessionId, long playerId) {
        return ClientBuilder.newClient(new ClientConfig())
                .target(serverConnection).path("api/sessions/" + sessionId + "/players/" + playerId)
                .request(APPLICATION_JSON)
                .accept(APPLICATION_JSON)
                .delete(Player.class);
    }

    /**
     * Retrieve all players from a session in the DB.
     *
     * @param sessionId the id of the session
     * @return List of all players from a session
     */
    public List<Player> getPlayers(long sessionId) {
        return ClientBuilder.newClient(new ClientConfig())
                .target(serverConnection).path("api/sessions/" + sessionId + "/players")
                .request(APPLICATION_JSON)
                .accept(APPLICATION_JSON)
                .get(new GenericType<List<Player>>() {
                });
    }

    /**
     * Retrieve all removed players from a session in the DB.
     *
     * @param sessionId the id of the session
     * @return List of all removed players from a session
     */
    public List<Player> getRemovedPlayers(long sessionId) {
        return ClientBuilder.newClient(new ClientConfig())
                .target(serverConnection).path("api/sessions/" + sessionId + "/removedPlayers")
                .request(APPLICATION_JSON)
                .accept(APPLICATION_JSON)
                .get(new GenericType<List<Player>>() {
                });
    }


    /**
     * Updates a session's number of time jokers
     *
     * @param sessionID  Session to update
     * @param timeJokers new number of timeJokers to be set
     * @return integer for number of jokers in use now
     */
    public Integer updateTimeJokers(long sessionID, int timeJokers) {
        return ClientBuilder.newClient(new ClientConfig())
                .target(serverConnection).path("api/sessions/" + sessionID + "/timeJokers/" + timeJokers)
                .request(APPLICATION_JSON)
                .accept(APPLICATION_JSON)
                .get(Integer.class);
    }

    /**
     * Resets a session's questionCounter
     *
     * @param sessionId The id of the session
     * @return The updated session
     */
    public GameSession resetQuestionCounter(long sessionId) {
        return ClientBuilder.newClient(new ClientConfig())
                .target(serverConnection).path("api/sessions/" + sessionId + "/reset")
                .request(APPLICATION_JSON)
                .accept(APPLICATION_JSON)
                .get(new GenericType<GameSession>() {
                });
    }

    /**
     * Check if the given username is used in an active session
     *
     * @param username The username to check
     * @return True if the username is used, otherwise false
     */
    public Boolean isDuplInActive(String username) {
        return ClientBuilder.newClient(new ClientConfig())
                .target(serverConnection).path("api/sessions/checkUsername/" + username)
                .request(APPLICATION_JSON)
                .accept(APPLICATION_JSON)
                .get(new GenericType<Boolean>() {
                });
    }

    /**
     * Retrieve all used jokers from a session in the DB.
     *
     * @param sessionId the id of the session
     * @return List of all used jokers from a session
     */
    public List<Joker> getUsedJoker(long sessionId) {
        return ClientBuilder.newClient(new ClientConfig())
                .target(serverConnection).path("api/sessions/" + sessionId + "/jokers")
                .request(APPLICATION_JSON)
                .accept(APPLICATION_JSON)
                .get(new GenericType<List<Joker>>() {
                });
    }

    /**
     * Adds an usedJoker to a game session.
     *
     * @param sessionId id of the session to add the joker to
     * @param joker     Joker object to be added
     * @return The joker that has been added
     */
    public Joker addUsedJoker(long sessionId, Joker joker) {
        return ClientBuilder.newClient(new ClientConfig())
                .target(serverConnection).path("api/sessions/" + sessionId + "/add/joker")
                .request(APPLICATION_JSON)
                .accept(APPLICATION_JSON)
                .post(Entity.entity(joker, APPLICATION_JSON), Joker.class);
    }

    /**
     * Sets the game rounds for a session.
     *
     * @param sessionId The id of the session.
     * @param rounds    The rounds to be set.
     * @return The updated session.
     */
    public GameSession setGameRounds(long sessionId, int rounds) {
        return ClientBuilder.newClient(new ClientConfig())
                .target(serverConnection).path("api/sessions/" + sessionId + "/rounds")
                .request(APPLICATION_JSON)
                .accept(APPLICATION_JSON)
                .put(Entity.entity(rounds, APPLICATION_JSON), GameSession.class);
    }
}
