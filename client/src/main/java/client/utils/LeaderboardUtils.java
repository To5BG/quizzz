package client.utils;

import commons.Player;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.core.GenericType;
import org.glassfish.jersey.client.ClientConfig;

import java.util.List;

import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON;

public class LeaderboardUtils {

    private static final String SERVER = "http://localhost:8080/";

    public List<Player> getAllLeaderBoardPlayers() {
        return ClientBuilder.newClient(new ClientConfig())
                .target(SERVER).path("api/leaderboard/")
                .request(APPLICATION_JSON)
                .accept(APPLICATION_JSON)
                .get(new GenericType<List<Player>>() {
                });
    }

    /**
     * Get all player object entries from the database
     * for single mode
     *
     * @return List of all player entries
     */
    public List<Player> getPlayerSingleScore() {
        return ClientBuilder.newClient(new ClientConfig())
                .target(SERVER).path("api/leaderboard/single")
                .request(APPLICATION_JSON)
                .accept(APPLICATION_JSON)
                .get(new GenericType<List<Player>>() {
                });
    }

    /**
     * Get all player object entries from the database
     * for multi mode
     *
     * @return List of all player entries
     */
    public List<Player> getPlayerMultiScore() {
        return ClientBuilder.newClient(new ClientConfig())
                .target(SERVER).path("api/leaderboard/multi")
                .request(APPLICATION_JSON)
                .accept(APPLICATION_JSON)
                .get(new GenericType<List<Player>>() {
                });
    }

    /**
     * Retrieve a player entry from the database by ID
     *
     * @param playerId Id of player to fetch
     * @return Player entry reference, if one with the given id exists
     */
    public Player getPlayerByIdInLeaderboard(long playerId) {
        return ClientBuilder.newClient(new ClientConfig())
                .target(SERVER).path("api/leaderboard/" + playerId)
                .request(APPLICATION_JSON)
                .accept(APPLICATION_JSON)
                .get(new GenericType<Player>() {
                });
    }

    /**
     * Adds a player entry to the database forcibly (without an associated game session)
     *
     * @param player the player to be added
     * @return a message to show whether the adding is successful or not
     */
    public Player addPlayerForcibly(Player player) {
        return ClientBuilder.newClient(new ClientConfig())
                .target(SERVER).path("api/leaderboard")
                .request(APPLICATION_JSON)
                .accept(APPLICATION_JSON)
                .post(Entity.entity(player, APPLICATION_JSON), Player.class);
    }

    /**
     * Update a player's score in DB
     *
     * @param playerId    Id of player
     * @param points      Updated points
     * @param isBestScore Is this over the current best score for the player
     * @return Updated player DB entry reference
     */
    public Player updateSingleScore(long playerId, int points, boolean isBestScore) {
        return ClientBuilder.newClient(new ClientConfig())
                .target(SERVER).path("api/leaderboard/" + playerId +
                        ((isBestScore) ? "/best" : "/") + "singlescore")
                .request(APPLICATION_JSON)
                .accept(APPLICATION_JSON)
                .put(Entity.entity(points, APPLICATION_JSON), Player.class);
    }

    /**
     * Update a player's score in DB
     *
     * @param playerId    Id of player
     * @param points      Updated points
     * @param isBestScore Is this over the current best score for the player
     * @return Updated player DB entry reference
     */
    public Player updateMultiScore(long playerId, int points, boolean isBestScore) {
        return ClientBuilder.newClient(new ClientConfig())
                .target(SERVER).path("api/leaderboard/" + playerId +
                        ((isBestScore) ? "/best" : "/") + "multiscore")
                .request(APPLICATION_JSON)
                .accept(APPLICATION_JSON)
                .put(Entity.entity(points, APPLICATION_JSON), Player.class);
    }

}
