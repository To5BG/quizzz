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

import com.google.inject.Inject;
import client.utils.ServerUtils;
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

    private final ServerUtils server;
    private final MainCtrl mainCtrl;
    private long playerId;
    private final long WAITING_AREA_ID = 1L;

    private ObservableList<Player> data;

    @FXML
    private TableView<Player> currentPlayers;
    @FXML
    private TableColumn<Player, String> userName;
    @FXML
    private Label playerText;
    @FXML
    private Button readyButton;


    @Inject
    public WaitingAreaCtrl(ServerUtils server, MainCtrl mainCtrl) {
        this.mainCtrl = mainCtrl;
        this.server = server;
        // Set to defaults
        this.playerId = 0L;
    }

    @Override
    public void initialize(URL loc, ResourceBundle res) {
        userName.setCellValueFactory(p -> new SimpleStringProperty(p.getValue().username));
        readyButton.setVisible(false);
    }

    /**
     * Called if controller is closed forcibly
     */
    public void shutdown() {
        if (readyButton.getText().equals("Not Ready")) server.toggleReady(WAITING_AREA_ID, false);
        server.removePlayer(WAITING_AREA_ID, playerId);
        setPlayerId(0L);
    }

    /**
     * Reverts the player to the splash screen and remove him from the current game session.
     */
    public void back() {
        if (readyButton.getText().equals("Not Ready")) server.toggleReady(WAITING_AREA_ID, false);
        server.removePlayer(WAITING_AREA_ID, playerId);
        setPlayerId(0L);
        readyButton.setText("Ready");
        mainCtrl.showSplash();
    }

    /**
     * Toggles between ready and not-ready state, modifying readyButton and the waiting area's playerReady count
     */
    public void toggleReady() {
        switch (readyButton.getText()) {
            case "Ready" -> {
                readyButton.setText("Not Ready");
                server.toggleReady(WAITING_AREA_ID, true);
            }
            case "Not Ready" -> {
                readyButton.setText("Ready");
                server.toggleReady(WAITING_AREA_ID, false);
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
     * @return True iff the refresh should continue
     */
    public boolean refresh() {
        GameSession waitingArea = server.getSession(WAITING_AREA_ID);
        data = FXCollections.observableList(waitingArea.players);
        currentPlayers.setItems(data);

        if (waitingArea.sessionStatus.equals("transferring")) {

            server.toggleReady(WAITING_AREA_ID, false);
            GameSession sessionToJoin = server.getAvailableSession();
            if (sessionToJoin == null) sessionToJoin = server.addSession(new GameSession("multiplayer"));

            readyButton.setText("Ready");
            readyButton.setVisible(false);

            server.addPlayer(sessionToJoin.id, server.removePlayer(WAITING_AREA_ID, playerId));
            if (server.getPlayers(WAITING_AREA_ID).size() == 0) {
                sessionToJoin = server.updateStatus(sessionToJoin, "ongoing");
                server.updateStatus(waitingArea, "waiting_area");
            }
            mainCtrl.showMultiplayer(sessionToJoin.id, playerId);
            return false;
        }

        int playersReady = waitingArea.playersReady;
        int playersCount = waitingArea.players.size();

        readyButton.setVisible(playersCount >= 2);
        playerText.setText("Ready: " + playersReady + "/" + playersCount);
        if (playersReady == playersCount && playersCount >= 2) {
            server.updateStatus(waitingArea, "transferring");
        }
        return true;
    }

    /**
     * Setter for playerId.
     *
     * @param playerId
     */
    public void setPlayerId(long playerId) {
        this.playerId = playerId;
    }
}