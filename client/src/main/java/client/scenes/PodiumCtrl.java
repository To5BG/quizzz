package client.scenes;

import client.utils.GameSessionUtils;
import com.google.inject.Inject;
import commons.Player;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Label;

import java.util.Collections;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class PodiumCtrl extends SceneCtrl {

    private final GameSessionUtils gameSessionUtils;
    private final MainCtrl mainCtrl;

    private long playerId;
    private long sessionId;

    @FXML
    private Label point3;
    @FXML
    private Label name3;
    @FXML
    private Label point2;
    @FXML
    private Label name2;
    @FXML
    private Label point1;
    @FXML
    private Label name1;
    @FXML
    private Label countdown;

    private List<Player> playerList;

    @Inject
    public PodiumCtrl(GameSessionUtils gameSessionUtils, MainCtrl mainCtrl) {
        this.gameSessionUtils = gameSessionUtils;
        this.mainCtrl = mainCtrl;
    }

    /**
     * the method to create a podium with a given game session
     *
     * @param sessionId the id of the given session
     */
    public void createPodium(long sessionId) {
        playerList = gameSessionUtils.getPlayers(sessionId);
        this.sessionId = sessionId;

        Collections.sort(playerList, (p1, p2) -> p2.getCurrentPoints() - p1.getCurrentPoints());

        countdown();
        var player1 = playerList.get(0);
        name1.setText(player1.username);
        point1.setText(String.valueOf(player1.currentPoints));
        var player2 = playerList.get(1);
        name2.setText(player2.username);
        point2.setText(String.valueOf(player2.currentPoints));
        if (playerList.size() > 2) {
            var player3 = playerList.get(2);
            name3.setText(player3.username);
            point3.setText(String.valueOf(player3.currentPoints));
        } else {
            name3.setText("");
            point3.setText("");
        }

    }

    /**
     * {@inheritDoc}
     */
    public void shutdown() {
        gameSessionUtils.removePlayer(sessionId, playerId);
    }

    /**
     * {@inheritDoc}
     */
    public void back() {
        shutdown();
    }

    /**
     * Setter for playerId
     *
     * @param sessionId - Id for the player
     */
    public void setSessionId(long sessionId) {
        this.sessionId = sessionId;
    }

    /**
     * Setter for playerId
     *
     * @param playerId - Id for the player
     */
    public void setPlayerId(long playerId) {
        this.playerId = playerId;
    }

    /**
     * the method to countdown when showing the podium screen
     */
    public void countdown() {
        new Timer().scheduleAtFixedRate(new TimerTask() {
            int counter = 10;

            @Override
            public void run() {
                Platform.runLater(() -> {
                    if (counter < 0) {
                        cancel();
                    } else {
                        countdown.setText("The end game screen will appear in " + counter + " sec");
                        counter--;
                    }
                });
            }
        }, 0, 1000);
    }
}
