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
import javafx.fxml.FXML;
import javafx.scene.control.TextField;

import java.util.List;

public class SplashCtrl {

    private final ServerUtils server;
    private final MainCtrl mainCtrl;

    @FXML
    private TextField usernameField;

    @Inject
    public SplashCtrl(ServerUtils server, MainCtrl mainCtrl) {
        this.server = server;
        this.mainCtrl = mainCtrl;
    }

    /*
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        colFirstName.setCellValueFactory(q -> new SimpleStringProperty(q.getValue().person.firstName));
        colLastName.setCellValueFactory(q -> new SimpleStringProperty(q.getValue().person.lastName));
        colQuote.setCellValueFactory(q -> new SimpleStringProperty(q.getValue().quote));
    }
    */

    /**
     * Initialize setup for main controller's showMultiplayer() method. Creates a new session if no free session is
     * available and adds the player to the session.
     */
    public void enterMultiplayerGame() {
        GameSession sessionToJoin = server.getAvailableSession();
        String newUserName = usernameField.getText();

        server.addPlayer(sessionToJoin.id, new Player(newUserName));
        var playerId = server
                .getPlayers(sessionToJoin.id)
                .stream().filter(p -> p.username.equals(newUserName))
                .findFirst().get().id;
        mainCtrl.showMultiplayer(sessionToJoin.id, playerId);
    }

    /**
     * Initialize setup for main controller's showMultiplayer() method. Creates a new session if no free session is
     * available and adds the player to the session.
     */
    public void showWaitingArea() {
        String newUserName = usernameField.getText();

        server.addPlayer(1L /*waiting area id*/, new Player(newUserName));
        var playerId = server
                .getPlayers(1L)
                .stream().filter(p -> p.username.equals(newUserName))
                .findFirst().get().id;
        mainCtrl.showWaitingArea(playerId);
    }

    /**
     * Initialize setup for main controller's showSinglePlayer() method.
     */
    public void showSinglePlayer() {
        String newUserName = usernameField.getText();
        GameSession newSession = new GameSession("multiplayer",
                List.of(new Player(newUserName)));
        newSession = server.addSession(newSession);
        var playerId = server
                .getPlayers(newSession.id)
                .stream().filter(p -> p.username.equals(newUserName))
                .findFirst().get().id;

        mainCtrl.showSinglePlayer(newSession.id, playerId);
    }

    /**
     * Initialize setup for main controller's showLeaderboard() method.
     */
    public void showLeaderboard() {
        mainCtrl.showLeaderboard();
    }

}