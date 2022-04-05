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
import client.utils.LongPollingUtils;
import com.google.inject.Inject;
import commons.GameSession;
import commons.Player;
import javafx.application.Platform;
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
import java.util.stream.Collectors;

public class WaitingAreaCtrl extends SceneCtrl implements Initializable {

    private final GameSessionUtils gameSessionUtils;
    private final LongPollingUtils longPollUtils;
    private final MainCtrl mainCtrl;

    private long playerId;
    private long waitingId;
    private int playerCount;

    @FXML
    private TableView<String> currentPlayers;
    @FXML
    private TableColumn<String, String> userName;
    @FXML
    private Label playerText;
    @FXML
    private Button readyButton;
    @FXML
    private Button backButton;


    @Inject
    public WaitingAreaCtrl(GameSessionUtils gameSessionUtils, LongPollingUtils longPollUtils, MainCtrl mainCtrl) {
        this.gameSessionUtils = gameSessionUtils;
        this.longPollUtils = longPollUtils;
        this.mainCtrl = mainCtrl;
        // Set to defaults
        this.playerId = 0L;
        this.playerCount = 0;
    }

    @Override
    public void initialize(URL loc, ResourceBundle res) {
        userName.setCellValueFactory(p -> new SimpleStringProperty(p.getValue()));
        readyButton.setVisible(false);
    }

    /**
     * {@inheritDoc}
     */
    public void shutdown() {
        if (readyButton.getText().equals("Not Ready")) gameSessionUtils.toggleReady(waitingId, false);
        Player player = gameSessionUtils.removePlayer(waitingId, playerId);
        gameSessionUtils.addPlayer(MainCtrl.SELECTION_ID, player);
        backButton.setDisable(true);
        Platform.runLater(() -> longPollUtils.haltUpdates("waitingArea"));
        backButton.setDisable(false);
    }

    /**
     * {@inheritDoc}
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
                gameSessionUtils.toggleReady(waitingId, true);
            }
            case "Not Ready" -> {
                readyButton.setText("Ready");
                gameSessionUtils.toggleReady(waitingId, false);
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
     */
    public void refresh(String update) {
        if (update == null) {
            GameSession waitingArea = gameSessionUtils.getSession(waitingId);
            ObservableList<String> data = FXCollections.observableList(
                    waitingArea.players.stream().map(p -> p.username).collect(Collectors.toList()));
            currentPlayers.setItems(data);

            playerCount = waitingArea.players.size();
            readyButton.setVisible(playerCount >= 2);
            playerText.setText("Ready: " + waitingArea.playersReady.get() + "/" + playerCount);
            return;
        }

        String[] tokens = update.split(" ");
        String operation = tokens[0];
        String operand = tokens[1];

        String currText = playerText.getText();
        int joinedStartIndex = currText.indexOf("/");

        switch (operation) {
            case "removePlayer:" -> {
                playerText.setText(currText.substring(0, joinedStartIndex + 1) + (--playerCount));
                readyButton.setVisible(playerCount >= 2);
                currentPlayers.getItems().remove(operand);
            }
            case "addPlayer:" -> {
                playerText.setText(currText.substring(0, joinedStartIndex + 1) + (++playerCount));
                readyButton.setVisible(playerCount >= 2);
                currentPlayers.getItems().add(operand);
            }
            case "playerReady:" -> playerText.setText("Ready: " + operand + currText.substring(joinedStartIndex));
            case "started:" -> {
                playerText.setText("Ready: " + operand + "/" + operand);
                readyButton.setText("Ready");
                readyButton.setVisible(false);
                //delay blocking haltUpdates() until screen is shown
                Platform.runLater(() -> {
                    longPollUtils.haltUpdates("waitingArea");
                    gameSessionUtils.toggleReady(waitingId, false);
                });
                mainCtrl.showMultiplayer(waitingId, playerId);
            }
        }
    }

    /**
     * Setter for playerId.
     *
     * @param playerId New playerId
     */
    public void setPlayerId(long playerId) {
        this.playerId = playerId;
    }

    /**
     * Setter for waitingId.
     *
     * @param waitingId New waitingId
     */
    public void setWaitingId(long waitingId) {
        this.waitingId = waitingId;
    }

    /**
     * Registers clients for waiting area updates
     */
    public void registerForUpdates() {
        longPollUtils.registerForWaitingAreaUpdates(p -> Platform.runLater(() -> refresh(p)), waitingId);
    }
}