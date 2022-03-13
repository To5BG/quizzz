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
import java.util.Comparator;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class LeaderBoardCtrl implements Initializable {

    private final ServerUtils server;
    private final MainCtrl mainCtrl;

    private ObservableList<Player> data;

    @FXML
    private TableView<Player> allPlayers;
    @FXML
    private TableColumn<Player, String> colName;
    @FXML
    private TableColumn<Player, String> colPoint;

    /**
     * constructor of the leaderboard
     *
     * @param server   the server of the leaderboard
     * @param mainCtrl the mainCtrl of the leaderboard
     */
    @Inject
    public LeaderBoardCtrl(ServerUtils server, MainCtrl mainCtrl) {
        this.server = server;
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
        colName.setCellValueFactory(p -> new SimpleStringProperty(p.getValue().username));
        colPoint.setCellValueFactory(q -> new SimpleStringProperty(String.valueOf(q.getValue().bestSingleScore)));
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
            case ESCAPE:
                back();
                break;
        }
    }

    /**
     * refresh the screen to show the leaderboards
     */
    public void refreshSingle() {
        var players = server.getPlayerSingleScore();
        data = FXCollections.observableList(players);
        allPlayers.setItems(data);
    }

    /**
     * refresh the screen to show the leaderboards
     */
    public void refreshMulti() {
        var players = server.getPlayerMultiScore();
        data = FXCollections.observableList(players);
        allPlayers.setItems(data);
    }

    /**
     * Show MultiPlayerLeaderBoard
     */
    public void showMultiLeaderboard() {
        colPoint.setCellValueFactory(q ->  new SimpleStringProperty(String.valueOf(q.getValue().bestMultiScore)));
        data.stream().sorted(Comparator.comparing(Player::getBestMultiScore).reversed())
                .collect(Collectors.toList());
        allPlayers.setItems(data);
        for(Player p : data) {
            System.out.println(p.toString());
        }
        allPlayers.refresh();
    }

    /**
     * Show SinglePlayerLeaderBoard
     */
    public void showSingleLeaderBoard() {
        colPoint.setCellValueFactory(q ->  new SimpleStringProperty(String.valueOf(q.getValue().bestSingleScore)));
        data.stream().sorted(Comparator.comparing(Player::getBestSingleScore).reversed())
                .collect(Collectors.toList());
        allPlayers.setItems(data);
        allPlayers.refresh();
    }

}