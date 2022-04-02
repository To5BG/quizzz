package client.utils;

import commons.GameSession;
import commons.Player;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.core.GenericType;
import jakarta.ws.rs.core.Response;

import org.apache.commons.lang3.tuple.Pair;
import org.glassfish.jersey.client.ClientConfig;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON;

public class LongPollingUtils {

    public static String serverConnection = "http://localhost:8080/";
    static ExecutorService execLeaderboard, execSelectionRoom, execWaitingArea;
    static int leaderboardSelect, selRoomSelect, waitingAreaSelect;


    /**
     * Register client listener to receive leaderboard updates
     *
     * @param consumer Consumer object representing the client's request
     */
    public void registerForLeaderboardUpdates(Consumer<Pair<String, List<Player>>> consumer) {
        execLeaderboard = Executors.newSingleThreadExecutor();
        execLeaderboard.submit(() -> {
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
                System.out.println(update);
                consumer.accept(Pair.of(res.getHeaders().get("X-gamemodeType").toString(), update));
            }
        });
    }

    /**
     * Register client listener to receive selection room updates
     *
     * @param consumer Consumer object representing the client's request
     */
    public void registerForSelectionRoomUpdates(Consumer<Pair<String, GameSession>> consumer) {
        execSelectionRoom = Executors.newSingleThreadExecutor();
        execSelectionRoom.submit(() -> {
            while (!Thread.interrupted()) {
                var res = ClientBuilder.newClient(new ClientConfig())
                        .target(serverConnection).path("api/sessions/updates")
                        .request(APPLICATION_JSON)
                        .accept(APPLICATION_JSON)
                        .get(Response.class);
                System.out.println("polling selection room...");
                if (res.getStatus() == 204) continue;
                // TODO Handle atomic updates more discretely
                // this has to be addressed after Rithik's MR with the room selection screen
                var update = res.readEntity(GameSession.class);
                System.out.println(update);
                consumer.accept(Pair.of(res.getHeaders().get("X-operation").toString(), update));
            }
        });
    }

    /**
     * Register client listener to receive waiting area updates
     *
     * @param consumer Consumer object representing the client's request
     */
    public void registerForWaitingAreaUpdates(Consumer<String> consumer) {
        execWaitingArea = Executors.newSingleThreadExecutor();
        execWaitingArea.submit(() -> {
            while (!Thread.interrupted()) {
                var res = ClientBuilder.newClient(new ClientConfig())
                        .target(serverConnection).path("api/sessions/updates")
                        .request(APPLICATION_JSON)
                        .accept(APPLICATION_JSON)
                        .get(Response.class);
                System.out.println("polling...");
                if (res.getStatus() == 204) continue;
                var update = res.readEntity(String.class);
                System.out.println(update);
                consumer.accept(update);
            }
        });
    }

    /**
     * Halt long-polling for updates
     */
    public void haltUpdates(String scene) {
        try {
            switch (scene) {
                case "waitingArea" -> {
                    execWaitingArea.shutdownNow();
                    execWaitingArea.awaitTermination(2, TimeUnit.SECONDS);
                }
                case "selectionRoom" -> {
                    execSelectionRoom.shutdownNow();
                    execSelectionRoom.awaitTermination(2, TimeUnit.SECONDS);
                }
                case "leaderboard" -> {
                    execLeaderboard.shutdownNow();
                    execSelectionRoom.awaitTermination(2, TimeUnit.SECONDS);
                }
            }
        } catch (Exception ignored) {
        }
    }
}
