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

import client.utils.LeaderboardUtils;
import client.utils.LongPollingUtils;
import com.google.inject.Inject;
import commons.Player;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.input.KeyEvent;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import org.apache.commons.lang3.tuple.Pair;

public class LeaderBoardCtrl extends SceneCtrl implements Initializable {

    private final LeaderboardUtils leaderboardUtils;
    private final LongPollingUtils longPollUtils;
    private final MainCtrl mainCtrl;

    @FXML
    private TableView<Player> allPlayersSingleplayer;
    @FXML
    private TableColumn<Player, String> colNameSingleplayer;
    @FXML
    private TableColumn<Player, String> colPointSingleplayer;

    @FXML
    private TableView<Player> allPlayersMultiplayer;
    @FXML
    private TableColumn<Player, String> colNameMultiplayer;
    @FXML
    private TableColumn<Player, String> colPointMultiplayer;

    @FXML
    private Button singleLeaderboard;
    @FXML
    private Button multiLeaderboard;
    @FXML
    private Label leaderboardLabel;

    /**
     * constructor of the leaderboard
     *
     * @param leaderboardUtils the utils that retrieves information about leaderboard form the server
     * @param mainCtrl         the mainCtrl of the leaderboard
     */
    @Inject
    public LeaderBoardCtrl(LeaderboardUtils leaderboardUtils, LongPollingUtils longPollUtils,
                           MainCtrl mainCtrl) {
        this.leaderboardUtils = leaderboardUtils;
        this.longPollUtils = longPollUtils;
        this.mainCtrl = mainCtrl;
    }

    /**
     * the method to initialize the leaderboard
     *
     * @param location  the location of the files
     * @param resources the resources to be loaded when initializing
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        colNameSingleplayer.setCellValueFactory(p -> new SimpleStringProperty(p.getValue().username));
        colPointSingleplayer
                .setCellValueFactory(q -> new SimpleStringProperty(String.valueOf(q.getValue().bestSingleScore)));
        colNameMultiplayer.setCellValueFactory(p -> new SimpleStringProperty(p.getValue().username));
        colPointMultiplayer
                .setCellValueFactory(q -> new SimpleStringProperty(String.valueOf(q.getValue().bestMultiScore)));
        showSingleLeaderboard();
    }

    /**
     * {@inheritDoc}
     */
    public void shutdown() {
        longPollUtils.haltUpdates("leaderboard");
    }

    /**
     * {@inheritDoc}
     */
    public void back() {
        shutdown();
        mainCtrl.showSplash();
    }

    /**
     * the method to listen to the pressed keys
     *
     * @param e an event which represents a pressed key
     */
    public void keyPressed(KeyEvent e) {
        switch (e.getCode()) {
            case ESCAPE -> back();
            case DIGIT1 -> showSingleLeaderboard();
            case DIGIT2 -> showMultiLeaderboard();
        }
    }

    /**
     * Show MultiPlayerLeaderBoard
     */
    public void showMultiLeaderboard() {
        allPlayersSingleplayer.setOpacity(0);
        allPlayersMultiplayer.setOpacity(1);
        leaderboardLabel.setText("Leaderboard-Multi");
        singleLeaderboard.setText("Single");
    }

    /**
     * Show SinglePlayerLeaderBoard
     */
    public void showSingleLeaderboard() {
        allPlayersSingleplayer.setOpacity(1);
        allPlayersMultiplayer.setOpacity(0);
        leaderboardLabel.setText("Leaderboard-Single");
        multiLeaderboard.setText("Multi");
    }

    /**
     * Gets sorted player lists for initial display when entering leaderboard screen
     */
    private void fetchInitialValues() {
        var players = FXCollections.observableList(leaderboardUtils.getPlayerSingleScore());
        allPlayersSingleplayer.setItems(players);
        allPlayersSingleplayer.refresh();

        players = FXCollections.observableList(leaderboardUtils.getPlayerMultiScore());
        allPlayersMultiplayer.setItems(players);
        allPlayersMultiplayer.refresh();
    }

    /**
     * Refreshes leaderboard data
     */
    public void refresh(Pair<String, List<Player>> update) {
        if (update == null) {
            fetchInitialValues();
            return;
        }
        switch (update.getKey()) {
            case "[single]" -> {
                allPlayersSingleplayer.setItems(FXCollections.observableList(update.getValue()));
                allPlayersSingleplayer.refresh();
            }
            case "[multi]" -> {
                allPlayersMultiplayer.setItems(FXCollections.observableList(update.getValue()));
                allPlayersMultiplayer.refresh();
            }
        }
    }

    /**
     * Registers clients for leaderboard updates
     */
    public void registerForUpdates() {
        longPollUtils.registerForLeaderboardUpdates(this::refresh);
    }
}