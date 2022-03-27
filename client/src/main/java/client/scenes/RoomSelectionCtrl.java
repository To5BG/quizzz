package client.scenes;

import client.utils.GameSessionUtils;
import com.google.inject.Inject;
import commons.GameSession;
import commons.Player;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.input.KeyEvent;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class RoomSelectionCtrl implements Initializable {

    private final GameSessionUtils gameSessionUtils;
    private final MainCtrl mainCtrl;

    private long playerId;

    @FXML
    private TableView<GameSession> availableRooms;
    @FXML
    private TableColumn<GameSession, String> roomNumber;

    @Inject
    public RoomSelectionCtrl(GameSessionUtils gameSessionUtils, MainCtrl mainCtrl) {
        this.gameSessionUtils = gameSessionUtils;
        this.mainCtrl = mainCtrl;
    }

    @Override
    public void initialize(URL loc, ResourceBundle res) {
        roomNumber.setCellValueFactory(r -> new SimpleStringProperty("Room # " + String.valueOf(r.getValue().id)));
    }

    /**
     * Set the player to a new Player
     * @param playerId - new Id for the player
     */
    public void setPlayerId(long playerId) {
        this.playerId = playerId;
    }


    /**
     * Reverts the player to the splash screen.
     */
    public void back() {
        gameSessionUtils.removePlayer(MainCtrl.SELECTION_ID, playerId);
        mainCtrl.showSplash();
    }

    /**
     * Switch method that maps keyboard key presses to functions.
     * @param e KeyEvent to be switched
     */
    public void keyPressed(KeyEvent e) {
        switch (e.getCode()) {
            case ESCAPE -> back();
        }
    }

    /**
     * Refresh the list of available waiting rooms
     * @return True iff the refresh should continue
     */
    public void refresh() {
        List<GameSession> roomList = gameSessionUtils.getAvailableSessions();
        ObservableList<GameSession> data = FXCollections.observableList(roomList);
        availableRooms.setItems(data);
    }

    /**
     * Initialize setup for main controller's showMultiplayer() method. Creates a new session if no free session is
     * available and adds the player to the session.
     * In case a player enters a username already present in an active game session, or an invalid/blank username, they
     * are not added to the session, instead being prompted to change their username.
     */
    public void hostRoom() {
        Player player = gameSessionUtils.removePlayer(MainCtrl.SELECTION_ID, playerId);
        GameSession session = new GameSession(GameSession.SessionType.WAITING_AREA);
        session.addPlayer(player);
        session = gameSessionUtils.addWaitingRoom(session);
        long playerId = player.id;
        long waitingId = session.id;
        if (playerId == 0L) {
            playerId = gameSessionUtils
                    .getPlayers(waitingId)
                    .stream().filter(p -> p.username.equals(player.username))
                    .findFirst().get().id;
        }
        mainCtrl.showWaitingArea(playerId);
    }

}
