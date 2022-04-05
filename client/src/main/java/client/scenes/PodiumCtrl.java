package client.scenes;

import client.utils.GameSessionUtils;
import com.google.inject.Inject;
import commons.Player;
import javafx.fxml.FXML;
import javafx.scene.control.Label;

import java.util.Collections;
import java.util.List;

public class PodiumCtrl {

    private final GameSessionUtils gameSessionUtils;
    private final MainCtrl mainCtrl;

    private long playerId;

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

        Collections.sort(playerList, (p1, p2) -> p2.getCurrentPoints() - p1.getCurrentPoints());

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
     * Removes player from session. Also called if controller is closed forcibly
     */
    public void shutdown() {
        gameSessionUtils.removePlayer(MainCtrl.SELECTION_ID, playerId);
    }


    /**
     * Setter for playerId
     *
     * @param playerId - Id for the player
     */
    public void setPlayerId(long playerId) {
        this.playerId = playerId;
    }
}
