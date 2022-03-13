package client.scenes;

import client.utils.ServerUtils;
import commons.Player;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javax.inject.Inject;
import java.net.URL;
import java.util.ResourceBundle;

public class SingleplayerCtrl extends GameCtrl {

    @Inject
    public SingleplayerCtrl(ServerUtils server, MainCtrl mainCtrl) {
        super(server, mainCtrl);
    }

    private ObservableList<Player> data;

    @FXML
    private TableView<Player> allPlayers;
    @FXML
    private TableColumn<Player, String> colName;
    @FXML
    private TableColumn<Player, String> colPoint;

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
        startSingleEvaluation();
    }

    /**
     * Enables the doublePoints joker if still available and calls the overridden method
     */
    @Override
    public void loadAnswer() {
        if(doublePointsJoker) {
            disableButton(doublePointsButton, false);
        }
        super.loadAnswer();
    }
    /**
     * Reverts the player to the splash screen and remove him from the current game session.
     */
    @Override
    public void back() {
        super.back();
    }

    /**
     * Disable the jokers that do not work for single-player
     */
    public void disableSingleplayerJokers() {
        decreaseTimeJoker = false;
        disableButton(decreaseTimeButton, true);
    }

    /**
     * refresh the screen to show the leaderboards
     */
    public void refresh() {
        var players = server.getPlayerSingleScore();
        data = FXCollections.observableList(players);
        allPlayers.setItems(data);
    }
}
