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
import client.utils.LeaderboardUtils;
import com.google.inject.Inject;
import commons.GameSession;
import commons.Player;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.scene.text.Text;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Optional;
import java.util.Scanner;

public class SplashCtrl {

    private final GameSessionUtils gameSessionUtils;
    private final LeaderboardUtils leaderboardUtils;
    private final MainCtrl mainCtrl;

    @FXML
    private TextField usernameField;

    @FXML
    private Text duplUsername;

    @FXML
    private Text invalidUserName;

    @Inject
    public SplashCtrl(GameSessionUtils gameSessionUtils, LeaderboardUtils leaderboardUtils, MainCtrl mainCtrl) {
        this.gameSessionUtils = gameSessionUtils;
        this.leaderboardUtils = leaderboardUtils;
        this.mainCtrl = mainCtrl;
    }

    /**
     * Check whether a given username is valid or not. Valid usernames are non-empty and contain only letters and/or
     * numbers
     *
     * @param username - the username whose validity is to be determined
     * @return true if username is valid, false otherwise
     */
    public boolean isUsernameValid(String username) {
        if (username.isBlank()) return false;
        for (int i = 0; i < username.length(); i++) {
            if (!Character.isLetterOrDigit(username.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    /**
     * Generate the player object corresponding to the username
     * @param username The username of the player
     * @return Optional of Player that is set when the username can be used
     */
    private Optional<Player> generatePlayer(String username) {
        Player result = null;
        if (!isUsernameValid(username)) {
            invalidUserName.setOpacity(1);
            duplUsername.setOpacity(0);
            usernameField.clear();
        } else if (gameSessionUtils.isDuplInActive(username)) {
            invalidUserName.setOpacity(0);
            duplUsername.setOpacity(1);
            usernameField.clear();
        } else {
            saveUsername(usernameField.getText());

            invalidUserName.setOpacity(0);
            duplUsername.setOpacity(0);
            result = leaderboardUtils.getPlayerByUsername(username);

            if (result != null) {
                result.setCurrentPoints(0);
            } else {
                result = new Player(username, 0);
            }
        }

        return Optional.ofNullable(result);
    }

    /**
     * Initialize setup for main controller's showMultiplayer() method. Creates a new session if no free session is
     * available and adds the player to the session.
     * In case a player enters a username already present in an active game session, or an invalid/blank username, they
     * are not added to the session, instead being prompted to change their username.
     */
    public void showWaitingArea() {
        String newUserName = usernameField.getText();
        Optional<Player> playerResult = generatePlayer(newUserName);
        if (playerResult.isEmpty()) return;

        gameSessionUtils.addPlayer(MainCtrl.WAITING_AREA_ID, playerResult.get());
        long playerId = playerResult.get().id;

        if (playerId == 0L) {
            playerId = gameSessionUtils
                    .getPlayers(MainCtrl.WAITING_AREA_ID)
                    .stream().filter(p -> p.username.equals(newUserName))
                    .findFirst().get().id;
        }

        mainCtrl.showWaitingArea(playerId);
    }

    /**
     * Initialize setup for main controller's showSinglePlayer() method.
     * In case a player enters an invalid/blank username, or if the username is used in an active game session, they are
     * not added to the session, instead being prompted to change their username.
     */
    public void showSinglePlayer() {
        String newUserName = usernameField.getText();
        Optional<Player> playerResult = generatePlayer(newUserName);
        if (playerResult.isEmpty()) return;

        GameSession newSession = new GameSession(GameSession.SessionType.SINGLEPLAYER);
        newSession = gameSessionUtils.addSession(newSession);
        gameSessionUtils.addPlayer(newSession.id, playerResult.get());

        long playerId = playerResult.get().id;

        if (playerId == 0L) {
            playerId = gameSessionUtils
                    .getPlayers(newSession.id).get(0).id;
        }

        mainCtrl.showSinglePlayer(newSession.id, playerId);
    }

    /**
     * Save the username to a file so it can be retrieved after coming back to the game
     *
     * @param username - String to be saved inside the file
     */
    private void saveUsername(String username) {
        try {
            File file = new File("username.txt");
            file.createNewFile();
            FileWriter writer = new FileWriter(file);
            writer.write(username);
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Initialize setup for main controller's showLeaderboard() method.
     */
    public void showLeaderboard() {
        mainCtrl.showLeaderboard();
    }

    /**
     * Autofill the usernameField with the saved username from the 'username.txt' file, if it exists.g
     */
    public void retrieveSavedName() {
        try {
            File file = new File("username.txt");
            if (file.exists() && file.canRead()) {
                Scanner scanner = new Scanner(file);
                String name = scanner.next();
                if (name != null && name.length() > 0) {
                    usernameField.setText(name);
                }
                scanner.close();
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }
}
