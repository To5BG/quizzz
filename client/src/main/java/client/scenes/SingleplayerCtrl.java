package client.scenes;

import client.utils.GameSessionUtils;
import client.utils.LeaderboardUtils;
import client.utils.QuestionUtils;
import client.utils.WebSocketsUtils;
import commons.Joker;
import commons.Player;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

import javax.inject.Inject;
import java.net.URL;
import java.util.ResourceBundle;

public class SingleplayerCtrl extends GameCtrl {

    protected ObservableList<Player> data;
    @FXML
    protected TableView<Player> allPlayers;
    @FXML
    protected TableColumn<Player, String> colName;
    @FXML
    protected TableColumn<Player, String> colPoint;

    @Inject
    public SingleplayerCtrl(WebSocketsUtils webSocketsUtils, GameSessionUtils gameSessionUtils,
                            LeaderboardUtils leaderboardUtils, QuestionUtils questionUtils, MainCtrl mainCtrl) {
        super(webSocketsUtils, gameSessionUtils, leaderboardUtils, questionUtils, mainCtrl);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        colName.setCellValueFactory(p -> new SimpleStringProperty(p.getValue().username));
        colPoint.setCellValueFactory(q -> new SimpleStringProperty(String.valueOf(q.getValue().bestSingleScore)));
    }

    /**
     * Submit an answer to the server and start evaluation
     */
    @Override
    public void submitAnswer(boolean initiatedByTimer) {
        super.submitAnswer(initiatedByTimer);
        startEvaluation();
    }

    /**
     * Enables the doublePoints joker if still available and calls the overridden method
     */
    @Override
    public void loadAnswer() {
        disableButton(decreaseTimeButton, !decreaseTimeJoker);
        disableButton(doublePointsButton, !doublePointsJoker);
        super.loadAnswer();
    }

    /**
     * If the joker is active make the time joker -0.5 so the booster will work at half the normal speed
     * for a singleplayer game
     *
     * @return -0.5 if the joker has been used, or 0 otherwise
     */
    @Override
    public double getTimeJokers() {
        return gameSessionUtils.getSession(sessionId).getTimeJokers() * -0.5;
    }

    /**
     * This is an equivalent method to decreaseTime for Multiplayer
     */
    public void increaseTime() {
        decreaseTimeJoker = false;
        disableButton(decreaseTimeButton, true);
        gameSessionUtils.updateTimeJokers(sessionId, 1);
        String username = leaderboardUtils.getPlayerByIdInLeaderboard(playerId).getUsername();
        gameSessionUtils.addUsedJoker(sessionId, new Joker(username, "DecreaseTimeJoker"));
    }

    /**
     * refresh the screen to show the leaderboards
     */
    public void refresh() {
        var players = leaderboardUtils.getPlayerSingleScore();
        data = FXCollections.observableList(players);
        allPlayers.setItems(data);
    }

    @Override
    protected String getJokerDisplayName(String jokerName) {
        if (jokerName.equals("DecreaseTimeJoker")) return "Increase time";
        return super.getJokerDisplayName(jokerName);
    }

    /**
     * Sends player to splash screen, along with an alert that the game has ended, with their points total
     */
    @Override
    public void handleGamePodium() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        mainCtrl.addCSS(alert);
        alert.setTitle("Game Over");
        alert.setHeaderText("You have been redirected to the splash screen");
        alert.setContentText("Your score was : " + points);
        alert.show();
        back();
    }
}
