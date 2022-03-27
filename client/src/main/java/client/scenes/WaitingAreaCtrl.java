/*
 * Copyright 2021 Delft University of Technology
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.input.KeyEvent;

import java.net.URL;
import java.util.ResourceBundle;

public class WaitingAreaCtrl implements Initializable {

    private final GameSessionUtils gameSessionUtils;
    private final MainCtrl mainCtrl;

    private long playerId;

    @FXML
    private TableView<Player> currentPlayers;
    @FXML
    private TableColumn<Player, String> userName;
    @FXML
    private Label playerText;
    @FXML
    private Button readyButton;


    @Inject
    public WaitingAreaCtrl(GameSessionUtils gameSessionUtils, MainCtrl mainCtrl) {
        this.gameSessionUtils = gameSessionUtils;
        this.mainCtrl = mainCtrl;
        // Set to defaults
        this.playerId = 0L;
    }

    @Override
    public void initialize(URL loc, ResourceBundle res) {
        userName.setCellValueFactory(p -> new SimpleStringProperty(p.getValue().username));
        readyButton.setVisible(false);
    }

    /**
     * Removes player from session. Also called if controller is closed forcibly
     */
    public void shutdown() {
        if (readyButton.getText().equals("Not Ready")) gameSessionUtils.toggleReady(MainCtrl.WAITING_AREA_ID, false);
        Player player = gameSessionUtils.removePlayer(MainCtrl.WAITING_AREA_ID, playerId);
        gameSessionUtils.addPlayer(MainCtrl.SELECTION_ID, player);
        setPlayerId(0L);
    }

    /**
     * Reverts the player to the splash screen and remove him from the current game session.
     */
    public void back() {
        long id = playerId;
        shutdown();
        readyButton.setText("Ready");
        mainCtrl.showRoomSelection(id);
    }

    /**
     * Toggles between ready and not-ready state, modifying readyButton and the waiting area's playerReady count
     */
    public void toggleReady() {
        switch (readyButton.getText()) {
            case "Ready" -> {
                readyButton.setText("Not Ready");
                gameSessionUtils.toggleReady(MainCtrl.WAITING_AREA_ID, true);
            }
            case "Not Ready" -> {
                readyButton.setText("Ready");
                gameSessionUtils.toggleReady(MainCtrl.WAITING_AREA_ID, false);
            }
        }
    }

    /**
     * Switch method that maps keyboard key presses to functions.
     *
     * @param e KeyEvent to be switched
     */
    public void keyPressed(KeyEvent e) {
        switch (e.getCode()) {
            case ESCAPE -> back();
            case ENTER -> toggleReady();
        }
    }

    /**
     * Refreshes the multiplayer player board for the current session.
     *
     * @return True iff the refresh should continue
     */
    public boolean refresh() {
        GameSession waitingArea = gameSessionUtils.getSession(MainCtrl.WAITING_AREA_ID);
        ObservableList<Player> data = FXCollections.observableList(waitingArea.players);
        currentPlayers.setItems(data);

        int playersReady = waitingArea.playersReady.get();
        int playersCount = waitingArea.players.size();

        if (waitingArea.sessionStatus == GameSession.SessionStatus.TRANSFERRING) {
         //   GameSession sessionToJoin = gameSessionUtils.getAvailableSession();
            gameSessionUtils.toggleReady(MainCtrl.WAITING_AREA_ID, false);
        //    if (sessionToJoin == null) return true;

            readyButton.setText("Ready");
            readyButton.setVisible(false);

           // mainCtrl.showMultiplayer(sessionToJoin.id, playerId);
            return false;
        }
        readyButton.setVisible(playersCount >= 2);
        playerText.setText("Ready: " + playersReady + "/" + playersCount);
        return true;
    }

    /**
     * Setter for playerId.
     *
     * @param playerId New playerId
     */
    public void setPlayerId(long playerId) {
        this.playerId = playerId;
    }
}