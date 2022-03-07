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
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.scene.text.Text;

import java.util.Optional;

public class SplashCtrl {

    private final ServerUtils server;
    private final MainCtrl mainCtrl;

    @FXML
    private TextField usernameField;

    @FXML
    private Text duplUsername;

    @FXML
    private Text invalidUserName;

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

        server.addPlayer(sessionToJoin.id, new Player(newUserName, 0));
        var playerId = server
                .getPlayers(sessionToJoin.id)
                .stream().filter(p -> p.username.equals(newUserName))
                .findFirst().get().id;
        mainCtrl.showMultiplayer(sessionToJoin.id, playerId);
    }

    /**
     * Check whether a given username is valid or not. Valid usernames are non-empty and contain only letters and/or
     * numbers
     *
     * @param username - the username whose validity is to be determined
     * @return true if username is valid, false otherwise
     */
    public boolean isUsernameValid(String username) {
        if(username.isBlank()) return false;
        for (int i = 0; i < username.length(); i++) {
            if ((Character.isLetterOrDigit(username.charAt(i)) == false)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Checks active sessions in the DB if another Player entry with the same username is present.
     *
     * @param username username of the player to be checked
     * @return true if another Player with the same username exists
     */
    public boolean isDuplInActive(String username) {
        for(GameSession gs : server.getSessions()) {
            Optional<Player> existing = gs
                    .getPlayers()
                    .stream().filter(p -> p.username.equals(username))
                    .findFirst();

            if(existing.isPresent()) return true;
        }
        return false;
    }

    /**
     * Checks the player repository if another Player entry with the same username is present.
     *
     * @param username username of the player to be checked
     * @return true if another Player with the same username exists
     */
    public boolean isDuplInRepository(String username) {
        for(Player p : server.getPlayersFromRepository()) {
            if(p.username.equals(username)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Gets the player with the same username (if exists) from the player repository
     * @param username username of the player to be obtained
     * @return the player if it exists, null otherwise
     */
    public Player getDuplPlayer(String username) {
        for(Player p : server.getPlayersFromRepository()) {
            if(p.username.equals(username)) {
                return p;
            }
        }
        return null;
    }


    /**
     * Initialize setup for main controller's showMultiplayer() method. Creates a new session if no free session is
     * available and adds the player to the session.
     * In case a player enters a username already present in an active game session, or an invalid/blank username, they
     * are not added to the session, instead being prompted to change their username.
     */
    public void showWaitingArea() {
        String newUserName = usernameField.getText();

        if(isDuplInActive(newUserName)) {
            invalidUserName.setOpacity(0);
            duplUsername.setOpacity(1);
            usernameField.clear();
        }

        else if(!isUsernameValid(newUserName)) {
            duplUsername.setOpacity(0);
            invalidUserName.setOpacity(1);
            usernameField.clear();
        }

        else {
            duplUsername.setOpacity(0);
            invalidUserName.setOpacity(0);

            if(isDuplInRepository(newUserName)) {
                Player p = getDuplPlayer(newUserName);
                p.setPoint(0);
                server.addPlayer(1L, p);
            }
            else {
                server.addPlayer(1L /*waiting area id*/, new Player(newUserName, 0));
            }
            var playerId = server
                    .getPlayers(1L)
                    .stream().filter(p -> p.username.equals(newUserName))
                    .findFirst().get().id;
            mainCtrl.showWaitingArea(playerId);
        }
    }

    /**
     * Initialize setup for main controller's showSinglePlayer() method.
     * In case a player enters an invalid/blank username, or if the username is used in an active game session, they are
     * not added to the session, instead being prompted to change their username.
     */
    public void showSinglePlayer() {
        String newUserName = usernameField.getText();

        if(!isUsernameValid(newUserName)) {
            invalidUserName.setOpacity(1);
            duplUsername.setOpacity(0);
            usernameField.clear();
        }

        else if(isDuplInActive(newUserName)) {
            invalidUserName.setOpacity(0);
            duplUsername.setOpacity(1);
            usernameField.clear();
        }

        else {
            invalidUserName.setOpacity(0);
            duplUsername.setOpacity(0);
            if(isDuplInRepository(newUserName)) {
                getDuplPlayer(newUserName).setPoint(0);
            }
            GameSession newSession = new GameSession(GameSession.SessionType.SINGLEPLAYER);
            newSession = server.addSession(newSession);
            server.addPlayer(newSession.id, isDuplInRepository(newUserName) ?
                    getDuplPlayer(newUserName) : new Player(newUserName, 0));

            var playerId = server
                    .getPlayers(newSession.id)
                    .stream().filter(p -> p.username.equals(newUserName))
                    .findFirst().get().id;

            mainCtrl.showSinglePlayer(newSession.id, playerId);
        }
    }

    /**
     * Initialize setup for main controller's showLeaderboard() method.
     */
    public void showLeaderboard() {
        mainCtrl.showLeaderboard();
    }
}