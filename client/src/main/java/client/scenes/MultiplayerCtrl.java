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
import java.util.ResourceBundle;

public class MultiplayerCtrl implements Initializable {

    private final ServerUtils server;
    private final MainCtrl mainCtrl;
    private long sessionId;
    private long playerId;

    private ObservableList<Player> data;

    @FXML
    private TableView<Player> currentPlayers;
    @FXML
    private TableColumn<Player, String> userName;

    @Inject
    public MultiplayerCtrl(ServerUtils server, MainCtrl mainCtrl) {
        this.mainCtrl = mainCtrl;
        this.server = server;
        // Set to defaults
        this.sessionId = 0L;
        this.playerId = 0L;
    }

    @Override
    public void initialize(URL loc, ResourceBundle res) {
        userName.setCellValueFactory(p -> new SimpleStringProperty(p.getValue().username));
    }

    public void shutdown() {
        if (sessionId != 0) server.removePlayer(sessionId, playerId);
    }

    /**
     * Reverts the player to the splash screen and remove him from the current game session.
     */
    public void back() {
        server.removePlayer(sessionId, playerId);
        var session = server.getSession(sessionId);
        if (session.players.size() == 0) server.removeSession(sessionId);
        sessionId = playerId = 0L;
        mainCtrl.showSplash();
    }

    /**
     * Switch method that maps keyboard key presses to functions.
     *
     * @param e KeyEvent to be switched
     */
    public void keyPressed(KeyEvent e) {
        switch (e.getCode()) {
            case ESCAPE:
                back();
                break;
        }
    }

    /**
     * Refreshes the multiplayer player board for the current session.
     */
    public boolean refresh() {
        var players = server.getPlayers(sessionId);
        data = FXCollections.observableList(players);
        currentPlayers.setItems(data);
        return true;
    }

    /**
     * Setter for sessionId.
     *
     * @param sessionId
     */
    public void setSessionId(long sessionId) {
        this.sessionId = sessionId;
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