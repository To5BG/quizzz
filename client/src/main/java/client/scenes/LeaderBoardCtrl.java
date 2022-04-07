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
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyEvent;
import javafx.util.Callback;

import java.net.URL;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import org.apache.commons.lang3.tuple.Pair;

public class LeaderBoardCtrl extends SceneCtrl implements Initializable {

    private final LeaderboardUtils leaderboardUtils;
    private final LongPollingUtils longPollUtils;
    private final MainCtrl mainCtrl;
    private List<Image> batteries;

    // Separate statistics to different tables - reduce client processing overhead
    @FXML
    private TableView<Player> allPlayersSingleplayer;
    @FXML
    private TableColumn<Player, Integer> colBatterySingleplayer;
    @FXML
    private TableColumn<Player, String> colNameSingleplayer;
    @FXML
    private TableColumn<Player, String> colPointSingleplayer;

    @FXML
    private TableView<Player> allPlayersMultiplayer;
    @FXML
    private TableColumn<Player, Integer> colBatteryMultiplayer;
    @FXML
    private TableColumn<Player, String> colNameMultiplayer;
    @FXML
    private TableColumn<Player, String> colPointMultiplayer;

    @FXML
    private TableView<Player> allPlayersSurvival;
    @FXML
    private TableColumn<Player, Integer> colBatterySurvival;
    @FXML
    private TableColumn<Player, String> colNameSurvival;
    @FXML
    private TableColumn<Player, String> colPointSurvival;

    @FXML
    private TableView<Player> allPlayersTimeAttack;
    @FXML
    private TableColumn<Player, Integer> colBatteryTimeAttack;
    @FXML
    private TableColumn<Player, String> colNameTimeAttack;
    @FXML
    private TableColumn<Player, String> colPointTimeAttack;

    @FXML
    private Button singleButton;
    @FXML
    private Button multiButton;
    @FXML
    private Button survivalButton;
    @FXML
    private Button timeAttackButton;
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
        ClassLoader cl = getClass().getClassLoader();
        batteries = new ArrayList<>();
        for (int i = 0; i < 9; ++i) {
            URL location = cl.getResource(
                    Path.of("", "Image", "decoration" + (7 + i) + ".png").toString());

            batteries.add(new Image(location.toString()));
        }

    }

    /**
     * the method to initialize the leaderboard
     *
     * @param location  the location of the files
     * @param resources the resources to be loaded when initializing
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {

        colBatterySingleplayer.setCellFactory(getBatteryCellFactory());
        colNameSingleplayer.setCellValueFactory(p -> new SimpleStringProperty(p.getValue().username));
        colPointSingleplayer
                .setCellValueFactory(q -> new SimpleStringProperty(String.valueOf(q.getValue().bestSingleScore)));

        colBatteryMultiplayer.setCellFactory(getBatteryCellFactory());
        colNameMultiplayer.setCellValueFactory(p -> new SimpleStringProperty(p.getValue().username));
        colPointMultiplayer
                .setCellValueFactory(q -> new SimpleStringProperty(String.valueOf(q.getValue().bestMultiScore)));

        colBatterySurvival.setCellFactory(getBatteryCellFactory());
        colNameSurvival.setCellValueFactory(p -> new SimpleStringProperty(p.getValue().username));
        colPointSurvival
                .setCellValueFactory(q -> new SimpleStringProperty(String.valueOf(q.getValue().bestSurvivalScore)));

        colBatteryTimeAttack.setCellFactory(getBatteryCellFactory());
        colNameTimeAttack.setCellValueFactory(p -> new SimpleStringProperty(p.getValue().username));
        colPointTimeAttack
                .setCellValueFactory(q -> new SimpleStringProperty(String.valueOf(q.getValue().bestTimeAttackScore)));

        singleButton.setOnAction(e -> showLeaderboard("single"));
        multiButton.setOnAction(e -> showLeaderboard("multi"));
        survivalButton.setOnAction(e -> showLeaderboard("survival"));
        timeAttackButton.setOnAction(e -> showLeaderboard("timeAttack"));

        showLeaderboard("single");
    }

    /**
     * Getter for a cell factory with battery images
     * @return Callback object to assign as a cell factory for the battery column
     */
    public Callback<TableColumn<Player, Integer>, TableCell<Player, Integer>> getBatteryCellFactory() {
        return new Callback<>() {
            @Override
            public TableCell<Player, Integer> call(TableColumn<Player, Integer> param) {
                return new TableCell<>() {
                    @Override
                    protected void updateItem(Integer item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty) setGraphic(null);
                        else {
                            int rank = this.getTableRow().getIndex();
                            Image img = batteries.get(Math.min(rank, batteries.size() - 1));
                            ImageView display = new ImageView(img);
                            display.setFitHeight(26.0);
                            display.setFitWidth(50.0);
                            display.setPreserveRatio(true);
                            setGraphic(display);
                        }
                    }
                };
            }
        };
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
            case DIGIT1 -> showLeaderboard("single");
            case DIGIT2 -> showLeaderboard("multi");
            case DIGIT3 -> showLeaderboard("survival");
            case DIGIT4 -> showLeaderboard("timeAttack");
        }
    }

    /**
     * Shows proper leaderboard based on provided identifier
     *
     * @param leaderboard String identifier for leaderboard to toggle
     */
    public void showLeaderboard(String leaderboard) {
        allPlayersSingleplayer.setVisible(false);
        allPlayersMultiplayer.setVisible(false);
        allPlayersSurvival.setVisible(false);
        allPlayersTimeAttack.setVisible(false);
        switch (leaderboard) {
            case "single" -> {
                allPlayersSingleplayer.setVisible(true);
                leaderboardLabel.setText("Leaderboard-Singleplayer");
            }
            case "multi" -> {
                allPlayersMultiplayer.setVisible(true);
                leaderboardLabel.setText("Leaderboard-Multiplayer");
            }
            case "survival" -> {
                allPlayersSurvival.setVisible(true);
                leaderboardLabel.setText("Leaderboard-Survival");
            }
            case "timeAttack" -> {
                allPlayersTimeAttack.setVisible(true);
                leaderboardLabel.setText("Leaderboard-TimeAttack");
            }
        }
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

        players = FXCollections.observableList(leaderboardUtils.getPlayerSurvivalScore());
        allPlayersSurvival.setItems(players);
        allPlayersSurvival.refresh();

        players = FXCollections.observableList(leaderboardUtils.getPlayerTimeAttackScore());
        allPlayersTimeAttack.setItems(players);
        allPlayersTimeAttack.refresh();
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
            case "[survival]" -> {
                allPlayersSurvival.setItems(FXCollections.observableList(update.getValue()));
                allPlayersSurvival.refresh();
            }
            case "[timeAttack]" -> {
                allPlayersTimeAttack.setItems(FXCollections.observableList(update.getValue()));
                allPlayersTimeAttack.refresh();
            }
        }
    }

    /**
     * Show SurvivalLeaderBoard
     */
    public void registerForUpdates() {
        longPollUtils.registerForLeaderboardUpdates(this::refresh);
    }
}