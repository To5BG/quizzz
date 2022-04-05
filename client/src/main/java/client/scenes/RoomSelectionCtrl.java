package client.scenes;

import client.utils.GameSessionUtils;
import com.google.inject.Inject;
import commons.GameSession;
import commons.Player;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.input.KeyEvent;

import java.net.URL;
import java.util.*;
import java.util.stream.Collectors;

public class RoomSelectionCtrl implements Initializable {

    private final GameSessionUtils gameSessionUtils;
    private final MainCtrl mainCtrl;

    private long playerId;
    private boolean notCancel;

    @FXML
    private TableView<GameSession> availableRooms;
    @FXML
    private TableColumn<GameSession, String> roomNumber;
    @FXML
    private TextField gameID;

    @Inject
    public RoomSelectionCtrl(GameSessionUtils gameSessionUtils, MainCtrl mainCtrl) {
        this.gameSessionUtils = gameSessionUtils;
        this.mainCtrl = mainCtrl;
        notCancel = true;
    }

    @Override
    public void initialize(URL loc, ResourceBundle res) {
        roomNumber.setCellValueFactory(r -> new SimpleStringProperty("Room: " + r.getValue().id
                + " - Session Status: " + r.getValue().sessionStatus
                + " - Player(s) active: " + r.getValue().players.size()));
    }

    /**
     * Setter for playerId
     *
     * @param playerId - new Id for the player
     */
    public void setPlayerId(long playerId) {
        this.playerId = playerId;
    }

    /**
     * Removes player from the selection session
     */
    public void shutdown() {
        gameSessionUtils.removePlayer(MainCtrl.SELECTION_ID, playerId);
    }

    /**
     * Reverts the player to the splash screen.
     */
    public void back() {
        shutdown();
        mainCtrl.showSplash();
        notCancel = false;
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
    public boolean refresh() {
        List<GameSession> roomList = gameSessionUtils.getSessions()
                .stream()
                .filter(gs -> (gs.sessionType == GameSession.SessionType.MULTIPLAYER ||
                        gs.sessionType == GameSession.SessionType.WAITING_AREA))
                .collect(Collectors.toList());
        if (roomList.isEmpty()) {
            availableRooms.setPlaceholder(new Label("No games here, try hosting one instead..."));
            availableRooms.setItems(null);
            return notCancel;
        }
        var data = FXCollections.observableList(roomList);
        availableRooms.setItems(data);
        return notCancel;
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
        notCancel = false;
    }

    /**
     * Player is added to the selected session.
     */
    public void joinSelectedRoom() {
        if (availableRooms.getSelectionModel().getSelectedItem() == null) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("No room selected");
            alert.setHeaderText("You have not selected a room");
            alert.setContentText("Choose a room to continue");
            alert.show();
            return;
        }
        GameSession session = availableRooms.getSelectionModel().getSelectedItem();
        joinSession(session);
    }

    /**
     * Player is added to the session searched
     */
    public void joinSearchedRoom() {
        String sessionIdString = gameID.getText();
        if (!isSessionIdValid(sessionIdString)) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Invalid ID");
            alert.setHeaderText("You have entered an invalid game session ID");
            alert.setContentText("Please enter a valid session ID to continue");
            alert.show();
            return;
        }
        long sessionId = Long.parseLong(gameID.getText());
        GameSession session = gameSessionUtils.getSession(sessionId);
        joinSession(session);
    }

    /**
     * Checks whether the specified session ID is valid or not. It is valid if it is present in the list of available
     * rooms
     *
     * @param sessionIdString the session Id string to be checked
     * @return true iff valid, false otherwise
     */
    public boolean isSessionIdValid(String sessionIdString) {
        long sessionId;
        if (sessionIdString.isBlank()) return false;
        try {
            sessionId = Long.parseLong(sessionIdString);
        } catch (NumberFormatException e) {
            return false;
        }
        for (GameSession gs : availableRooms.getItems()) {
            if (gs.id == sessionId) return true;
        }
        return false;
    }

    /**
     * Player is added to random waiting area
     * or creates a new waiting area if there are none available.
     */
    public void quickJoin() {
        var listRooms = availableRooms.getItems();
        if (listRooms == null || listRooms.isEmpty()) {
            hostRoom();
            return;
        }

        int randomId = new Random().nextInt(listRooms.size());
        joinSession(listRooms.get(randomId));
    }

    /**
     * Finds the player ID of the player from the specified session using the username
     *
     * @param sessionId - session ID of the session
     * @param username  - username of the player whose ID is to be determined
     * @return player's ID
     */
    public long findPlayerIdByUsername(long sessionId, String username) {
        return gameSessionUtils
                .getPlayers(sessionId)
                .stream().filter(p -> p.username.equals(username))
                .findFirst().get().id;
    }

    /**
     * Adds the player to the specified game session
     *
     * @param session - session to join
     */
    private void addPlayerToSession(GameSession session) {
        Player player = gameSessionUtils.removePlayer(MainCtrl.SELECTION_ID, playerId);
        gameSessionUtils.addPlayer(session.id, player);
        if (playerId == 0L) playerId = findPlayerIdByUsername(session.id, player.username);
        notCancel = false;
    }

    /**
     * Player is added to the specified session if the game session is of the status Play Again or Waiting Room.
     * If not, the user is not added, simply alerted.
     *
     * @param session - GameSession to which the player is added.
     */
    public void joinSession(GameSession session) {
        switch (session.sessionStatus) {
            case WAITING_AREA:
                addPlayerToSession(session);
                mainCtrl.showWaitingArea(playerId, session.id);
                break;
            case PLAY_AGAIN:
                addPlayerToSession(session);
                mainCtrl.showEndGameScreen(session.id, playerId);
                break;
            default:
                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setTitle("Ongoing game");
                alert.setHeaderText("The selected game session is still going on");
                alert.setContentText("You can wait for it to get over, or join a new game");
                alert.show();
        }
    }

    /**
     * Setter for notCancel
     *
     * @param notCancel
     */
    public void setNotCancel(boolean notCancel) {
        this.notCancel = notCancel;
    }
}
