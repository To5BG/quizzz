package client.utils;

import commons.Player;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.core.GenericType;
import org.glassfish.jersey.client.ClientConfig;

import java.util.List;

import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON;

public class LeaderboardUtils {

    public static String serverConnection = "http://localhost:8080/";

    public List<Player> getAllLeaderBoardPlayers() {
        return ClientBuilder.newClient(new ClientConfig())
                .target(serverConnection).path("api/leaderboard/")
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
                .target(serverConnection).path("api/leaderboard/single")
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
                .target(serverConnection).path("api/leaderboard/multi")
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
                .target(serverConnection).path("api/leaderboard/" + playerId)
                .request(APPLICATION_JSON)
                .accept(APPLICATION_JSON)
                .get(new GenericType<Player>() {
                });
    }

    /**
     * Get the player object with the specified username
     * @param username The username of the player
     * @return The player object is the username if found, otherwise false
     */
    public Player getPlayerByUsername(String username) {
        return ClientBuilder.newClient(new ClientConfig())
                .target(serverConnection).path("api/leaderboard/getByUsername/" + username)
                .request(APPLICATION_JSON)
                .accept(APPLICATION_JSON)
                .get(new GenericType<Player>() {
                });
    }
}
