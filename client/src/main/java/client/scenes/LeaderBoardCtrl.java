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
import com.google.inject.Inject;
import commons.Player;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
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

public class LeaderBoardCtrl implements Initializable {

    private final LeaderboardUtils leaderboardUtils;
    private final MainCtrl mainCtrl;
    @FXML
    protected ImageView battery0;
    @FXML
    protected ImageView battery1;
    @FXML
    protected ImageView battery2;
    @FXML
    protected ImageView battery3;
    @FXML
    protected ImageView battery4;
    @FXML
    protected ImageView battery5;
    @FXML
    protected ImageView battery6;
    @FXML
    protected ImageView battery7;
    @FXML
    protected ImageView battery8;
    @FXML
    protected ImageView battery9;
    private ObservableList<Player> data;
    @FXML
    private TableView<Player> allPlayers;
    @FXML
    private TableColumn<Player, Integer> colBattery;
    @FXML
    private TableColumn<Player, String> colName;
    @FXML
    private TableColumn<Player, String> colPoint;
    @FXML
    private Button singleLeaderboard;
    @FXML
    private Button multiLeaderboard;
    @FXML
    private Button timeAttackButton;
    @FXML
    private Button survivalButton;
    @FXML
    private Label leaderboardLabel;
    private List<Image> batteries;


    /**
     * constructor of the leaderboard
     *
     * @param leaderboardUtils the utils that retrieves information about leaderboard form the server
     * @param mainCtrl         the mainCtrl of the leaderboard
     */
    @Inject
    public LeaderBoardCtrl(LeaderboardUtils leaderboardUtils, MainCtrl mainCtrl) {
        this.leaderboardUtils = leaderboardUtils;
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
        colName.setCellValueFactory(p -> new SimpleStringProperty(p.getValue().username));
        colPoint.setCellValueFactory(q -> new SimpleStringProperty(String.valueOf(q.getValue().bestSingleScore)));
        colBattery.setCellFactory(new Callback<>() {
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
        });

    }


    /**
     * Created for be 'Back' button which makes the player back to the splash screen
     */
    public void back() {
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
        }
    }

    /**
     * refresh the screen to show the Single Player leaderboards
     */
    public void refreshSingle() {
        var players = leaderboardUtils.getPlayerSingleScore();
        data = FXCollections.observableList(players);
        allPlayers.setItems(data);
        leaderboardLabel.setText("Leaderboard-Singleplayer");
    }

    /**
     * refresh the screen to show the Time Attack leaderboards
     */
    public void refreshTimeAttack() {
        var players = leaderboardUtils.getPlayerTimeAttackScore();
        data = FXCollections.observableList(players);
        allPlayers.setItems(data);
        leaderboardLabel.setText("Leaderboard-TimeAttack");
    }

    /**
     * refresh the screen to show the Survival leaderboards
     */
    public void refreshSurvival() {
        var players = leaderboardUtils.getPlayerSurvivalScore();
        data = FXCollections.observableList(players);
        allPlayers.setItems(data);
        leaderboardLabel.setText("Leaderboard-Survival");
    }

    /**
     * refresh the screen to show the Multiplayer leaderboards
     */
    public void refreshMulti() {
        var players = leaderboardUtils.getPlayerMultiScore();
        data = FXCollections.observableList(players);
        allPlayers.setItems(data);
        leaderboardLabel.setText("Leaderboard-Multiplayer");
    }

    /**
     * Show MultiPlayerLeaderBoard
     */
    public void showMultiLeaderboard() {
        colPoint.setCellValueFactory(q -> new SimpleStringProperty(String.valueOf(q.getValue().bestMultiScore)));
        refreshMulti();
        allPlayers.setItems(data);
        allPlayers.refresh();
        singleLeaderboard.setText("Singleplayer");
    }

    /**
     * Show SinglePlayerLeaderBoard
     */
    public void showSingleLeaderBoard() {
        colPoint.setCellValueFactory(q -> new SimpleStringProperty(String.valueOf(q.getValue().bestSingleScore)));
        refreshSingle();
        allPlayers.setItems(data);
        allPlayers.refresh();
        multiLeaderboard.setText("Multiplayer");
    }

    /**
     * Show TimeAttackLeaderBoard
     */
    public void showTimeAttackLeaderBoard() {
        colPoint.setCellValueFactory(q -> new SimpleStringProperty(String.valueOf(q.getValue().bestTimeAttackScore)));
        refreshTimeAttack();
        allPlayers.setItems(data);
        allPlayers.refresh();
        timeAttackButton.setText("Time Attack");
    }

    /**
     * Show SurvivalLeaderBoard
     */
    public void showSurvivalLeaderBoard() {
        colPoint.setCellValueFactory(q -> new SimpleStringProperty(String.valueOf(q.getValue().bestSurvivalScore)));
        refreshSurvival();
        allPlayers.setItems(data);
        allPlayers.refresh();
        survivalButton.setText("Survival");
    }
}