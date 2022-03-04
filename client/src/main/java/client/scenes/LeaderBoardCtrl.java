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
import commons.Point;
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

public class LeaderBoardCtrl implements Initializable {

    private final ServerUtils server;
    private final MainCtrl mainCtrl;

    private ObservableList<Point> data;

    @FXML
    private TableView<Point> allPoints;
    @FXML
    private TableColumn<Point, String> colName;
    @FXML
    private TableColumn<Point, String> colPoint;

    @Inject
    public LeaderBoardCtrl(ServerUtils server, MainCtrl mainCtrl) {
        this.server = server;
        this.mainCtrl = mainCtrl;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        colName.setCellValueFactory(p -> new SimpleStringProperty(p.getValue().player.getUsername()));
        colPoint.setCellValueFactory(q -> new SimpleStringProperty(String.valueOf(q.getValue().point)));
    }

    /**
     * copied from Alpi's work
     * Reverts the player to the splash screen and remove him from the current game session.
     */
    public void back() {
        mainCtrl.showSplash();
    }

    /**
     * copied from Alpi's work
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
     * refresh the screen to show the leaderboards
     */
    public void refresh() {
        var points = server.getPoints();
        data = FXCollections.observableList(points);
        allPoints.setItems(data);
    }

}