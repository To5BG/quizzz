package client.scenes;

import client.utils.GameSessionUtils;
import com.google.inject.Inject;
import commons.GameSession;
import commons.Player;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.input.KeyEvent;

import java.net.URL;
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
     *
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
     *
     * @param e KeyEvent to be switched
     */
    public void keyPressed(KeyEvent e) {
        switch (e.getCode()) {
            case ESCAPE -> back();
        }
    }

    /**
     * Refresh the list of available waiting rooms
     *
     * @return True iff the refresh should continue
     */
    public void refresh() {
        var roomList = gameSessionUtils.getAvailableSessions();
        if (roomList == null || roomList.isEmpty()) {
            availableRooms.setPlaceholder(new Label("No games here, try hosting one instead..."));
            availableRooms.setItems(null);
            return;
        }
        var data = FXCollections.observableList(roomList);
        availableRooms.setItems(data);
    }

    /**
     * Initialize setup for main controller's showWaitingArea() method. Creates a new session.
     */
    public void hostRoom() {
        Player player = gameSessionUtils.removePlayer(MainCtrl.SELECTION_ID, playerId);
        GameSession session = new GameSession(GameSession.SessionType.WAITING_AREA);
        session.addPlayer(player);
        session = gameSessionUtils.addWaitingRoom(session);
        long waitingId = session.id;
        mainCtrl.showWaitingArea(playerId, waitingId);
    }

    /**
     * Initialize setup for main controller's showWaitingArea() method. Player is added to the selected session.
     */
    public void joinRoom() {
        if (availableRooms.getSelectionModel().getSelectedItem() == null) {
            //TODO: Add some kind of alert to the user
            return;
        }
        GameSession session = availableRooms.getSelectionModel().getSelectedItem();
        Player player = gameSessionUtils.removePlayer(MainCtrl.SELECTION_ID, playerId);
        gameSessionUtils.addPlayer(session.id, player);
        long waitingId = session.id;
        if (playerId == 0L) {
            playerId = gameSessionUtils
                    .getPlayers(waitingId)
                    .stream().filter(p -> p.username.equals(player.username))
                    .findFirst().get().id;
        }
        mainCtrl.showWaitingArea(playerId, waitingId);
    }

}
