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
import client.utils.QuestionUtils;
import client.utils.WebSocketsUtils;
import com.google.inject.Inject;
import commons.Player;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.text.Text;
import javafx.util.Duration;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SplashCtrl implements Initializable {

    private final GameSessionUtils gameSessionUtils;
    private final LeaderboardUtils leaderboardUtils;
    private final QuestionUtils questionUtils;
    private final WebSocketsUtils webSocketsUtils;
    private final MainCtrl mainCtrl;
    private final GameAnimation gameAnimation;
    @FXML
    protected Button singleplayerButton;
    @FXML
    protected Button multiplayerButton;
    @FXML
    protected Button leaderboardButton;
    private String url;
    @FXML
    private TextField usernameField;
    @FXML
    private Text duplUsername;
    @FXML
    private Text invalidUserName;
    @FXML
    private TextField connectionField;
    @FXML
    private Text failedConnectionAlert;

    @Inject
    public SplashCtrl(GameSessionUtils gameSessionUtils, LeaderboardUtils leaderboardUtils,
                      QuestionUtils questionUtils, WebSocketsUtils webSocketsUtils, MainCtrl mainCtrl) {
        this.gameSessionUtils = gameSessionUtils;
        this.leaderboardUtils = leaderboardUtils;
        this.webSocketsUtils = webSocketsUtils;
        this.questionUtils = questionUtils;
        this.mainCtrl = mainCtrl;
        this.gameAnimation = new GameAnimation();
    }

    /**
     * Enable the animation for the gameButtons
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        gameAnimation.startBatteryAnimation(List.of(singleplayerButton, multiplayerButton, leaderboardButton));
    }

    /**
     * Sets the server path for all server utils to the provided url
     *
     * @return True iff the connection is successful
     */
    public boolean establishConnection() {
        String connURL = connectionField.getText().strip();

        if (connURL.isEmpty()) connURL = "http://localhost:8080/";
        if (!connURL.endsWith("/")) connURL = connURL.concat("/");

        Pattern connPattern = Pattern.compile("\\w+\\w+://[\\w.]+:\\d+/");
        Matcher matcher = connPattern.matcher(connURL);

        try {
            if (matcher.find()) connURL = matcher.group();
            URL url = new URL(connURL);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.connect();
            int respCode = connection.getResponseCode();
            if (respCode != 200) {
                alertFailedConnection();
                return false;
            }

            gameSessionUtils.serverConnection = connURL;
            leaderboardUtils.serverConnection = connURL;
            questionUtils.serverConnection = connURL;
            webSocketsUtils.updateConnection(connURL);
            this.url = connURL;
            return true;
        } catch (Exception e) {
            alertFailedConnection();
            return false;
        }
    }

    /**
     * Displays an alert for a failed attempt at connecting to the server.
     */
    public void alertFailedConnection() {
        connectionField.clear();
        failedConnectionAlert.setOpacity(1);
        Platform.runLater(() -> new Timeline(
                new KeyFrame(Duration.seconds(2), e -> failedConnectionAlert.setOpacity(0))).play());
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
     *
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
     * Initialize setup for main controller's showRoomSelection() method.
     * In case a player enters an invalid/blank username, or if the username is used in an active game session, they are
     * being prompted to change their username.
     */
    public void showRoomSelection() {
        if (!establishConnection()) return;
        String newUserName = usernameField.getText();
        Optional<Player> playerResult = generatePlayer(newUserName);
        if (playerResult.isEmpty()) return;

        gameSessionUtils.addPlayer(MainCtrl.SELECTION_ID, playerResult.get());
        long playerId = playerResult.get().id;

        if (playerId == 0L) {
            playerId = gameSessionUtils
                    .getPlayers(MainCtrl.SELECTION_ID)
                    .stream().filter(p -> p.username.equals(newUserName))
                    .findFirst().get().id;
        }

        mainCtrl.showRoomSelection(playerId);
    }

    /**
     * Initialize setup for main controller's showGamemodeScreen() method.
     * In case a player enters an invalid/blank username, or if the username is used in an active game session, they are
     * not added to the session, instead being prompted to change their username.
     */
    public void showGamemodeScreen() {
        if (!establishConnection()) return;
        String newUserName = usernameField.getText();
        Optional<Player> playerResult = generatePlayer(newUserName);
        if (playerResult.isEmpty()) return;

        gameSessionUtils.addPlayer(MainCtrl.SELECTION_ID, playerResult.get());
        long playerId = playerResult.get().id;

        if (playerId == 0L) {
            playerId = gameSessionUtils
                    .getPlayers(MainCtrl.SELECTION_ID)
                    .stream().filter(p -> p.username.equals(newUserName))
                    .findFirst().get().id;
        }

        mainCtrl.showGamemodeScreen(playerId);
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
        if (!establishConnection()) return;
        mainCtrl.showLeaderboard();
    }

    /**
     * Show the edit activities page
     */
    public void showWebView() {
        if (!establishConnection()) return;
        mainCtrl.showWebView(this.url);
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

    /**
     * Show the tutorial page
     */
    public void showTutorial() {
        mainCtrl.showTutorial();
    }


}
