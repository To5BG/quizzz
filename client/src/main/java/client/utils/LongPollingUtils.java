package client.utils;

import commons.Player;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.core.GenericType;
import jakarta.ws.rs.core.Response;

import org.apache.commons.lang3.tuple.Pair;
import org.glassfish.jersey.client.ClientConfig;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON;

public class LongPollingUtils {

    public static String serverConnection = "http://localhost:8080/";
    static ExecutorService exec = Executors.newSingleThreadExecutor();

    /**
     * Register client listener to receive singleplayer leaderboard updates
     *
     * @param consumer Consumer object representing the client's request
     */
    public void registerForLeaderboardUpdates(Consumer<Pair<String, List<Player>>> consumer) {
        exec = Executors.newSingleThreadExecutor();
        exec.submit(() -> {
            while (!Thread.interrupted()) {
                var res = ClientBuilder.newClient(new ClientConfig())
                        .target(serverConnection).path("api/leaderboard/updates")
                        .request(APPLICATION_JSON)
                        .accept(APPLICATION_JSON)
                        .get(Response.class);
                System.out.println("polling leaderboard...");
                if (res.getStatus() == 204) continue;
                List<Player> update = res.readEntity(
                        new GenericType<List<Player>>() {
                        });
                consumer.accept(Pair.of(res.getHeaders().get("X-gamemodeType").toString(), update));
            }
        });
    }

    /**
     * Register client listener to receive waiting area updates
     *
     * @param consumer Consumer object representing the client's request
     */
    public void registerForWaitingAreaUpdates(Consumer<String> consumer) {
        exec = Executors.newSingleThreadExecutor();
        exec.submit(() -> {
            while (!Thread.interrupted()) {
                var res = ClientBuilder.newClient(new ClientConfig())
                        .target(serverConnection).path("api/sessions/updates")
                        .request(APPLICATION_JSON)
                        .accept(APPLICATION_JSON)
                        .get(Response.class);
                System.out.println("polling...");
                if (res.getStatus() == 204) continue;
                var change = res.readEntity(String.class);
                consumer.accept(change);
            }
        });
    }

    /**
     * Halt long-polling for updates
     */
    public void haltUpdates() {
        exec.shutdownNow();
    }
}
