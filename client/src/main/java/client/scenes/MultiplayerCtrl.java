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
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.input.KeyEvent;
import java.util.ArrayList;

public class MultiplayerCtrl {

    private final ServerUtils server;
    private final MainCtrl mainCtrl;

    private ObservableList<Player> data;

    @FXML
    private TableView<Player> currentPlayers;
    @FXML
    private TableColumn<Player, String> userName;

    @Inject
    public MultiplayerCtrl(ServerUtils server, MainCtrl mainCtrl) {
        this.mainCtrl = mainCtrl;
        this.server = server;
    }

    public void back() {
        mainCtrl.showSplash();
    }

    public void keyPressed(KeyEvent e) {
        switch (e.getCode()) {
            case ESCAPE:
                back();
                break;
        }
    }

    public void refresh() {
        var players = server.getPlayers(1L);
        data = FXCollections.observableList(new ArrayList<>(players));
        currentPlayers.setItems(data);
    }
}