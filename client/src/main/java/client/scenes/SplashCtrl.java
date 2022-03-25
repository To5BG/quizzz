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
import commons.GameSession;
import commons.Player;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.scene.text.Text;
import javafx.util.Duration;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Optional;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SplashCtrl {

    private final GameSessionUtils gameSessionUtils;
    private final LeaderboardUtils leaderboardUtils;
    private final QuestionUtils questionUtils;
    private final WebSocketsUtils webSocketsUtils;
    private final MainCtrl mainCtrl;

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
     * Sets the server path for all server utils to the provided url
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
     * Initialize setup for main controller's showMultiplayer() method. Creates a new session if no free session is
     * available and adds the player to the session.
     */
    public void enterMultiplayerGame() {
        GameSession sessionToJoin = gameSessionUtils.getAvailableSession();
        String newUserName = usernameField.getText();

        gameSessionUtils.addPlayer(sessionToJoin.id, new Player(newUserName, 0));
        var playerId = gameSessionUtils
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
        if (username.isBlank()) return false;
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
        for (GameSession gs : gameSessionUtils.getSessions()) {
            Optional<Player> existing = gs
                    .getPlayers()
                    .stream().filter(p -> p.username.equals(username))
                    .findFirst();

            if (existing.isPresent()) return true;
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
        for (Player p : leaderboardUtils.getAllLeaderBoardPlayers()) {
            if (p.username.equals(username)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Gets the player with the same username (if exists) from the player repository
     *
     * @param username username of the player to be obtained
     * @return the player if it exists, null otherwise
     */
    public Player getDuplPlayer(String username) {
        for (Player p : leaderboardUtils.getAllLeaderBoardPlayers()) {
            if (p.username.equals(username)) {
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
        if (!establishConnection()) return;
        String newUserName = usernameField.getText();

        if (isDuplInActive(newUserName)) {
            invalidUserName.setOpacity(0);
            duplUsername.setOpacity(1);
            usernameField.clear();
        } else if (!isUsernameValid(newUserName)) {
            duplUsername.setOpacity(0);
            invalidUserName.setOpacity(1);
            usernameField.clear();
        } else {
            duplUsername.setOpacity(0);
            invalidUserName.setOpacity(0);

            if (isDuplInRepository(newUserName)) {
                Player p = getDuplPlayer(newUserName);
                p.setCurrentPoints(0);
                gameSessionUtils.addPlayer(1L, p);
            } else {
                gameSessionUtils.addPlayer(1L /*waiting area id*/, new Player(newUserName, 0));
            }
            var playerId = gameSessionUtils
                    .getPlayers(1L)
                    .stream().filter(p -> p.username.equals(newUserName))
                    .findFirst().get().id;
            try {
                saveUsername(usernameField.getText());
            } catch (IOException e) {
                e.printStackTrace();
            }
            mainCtrl.showWaitingArea(playerId);
        }
    }

    /**
     * Initialize setup for main controller's showSinglePlayer() method.
     * In case a player enters an invalid/blank username, or if the username is used in an active game session, they are
     * not added to the session, instead being prompted to change their username.
     */
    public void showSinglePlayer() {
        if (!establishConnection()) return;
        String newUserName = usernameField.getText();

        if (!isUsernameValid(newUserName)) {
            invalidUserName.setOpacity(1);
            duplUsername.setOpacity(0);
            usernameField.clear();
        } else if (isDuplInActive(newUserName)) {
            invalidUserName.setOpacity(0);
            duplUsername.setOpacity(1);
            usernameField.clear();
        } else {
            try {
                saveUsername(usernameField.getText());
            } catch (IOException e) {
                e.printStackTrace();
            }
            invalidUserName.setOpacity(0);
            duplUsername.setOpacity(0);
            if (isDuplInRepository(newUserName)) {
                getDuplPlayer(newUserName).setCurrentPoints(0);
            }
            GameSession newSession = new GameSession(GameSession.SessionType.SINGLEPLAYER);
            newSession = gameSessionUtils.addSession(newSession);
            gameSessionUtils.addPlayer(newSession.id, isDuplInRepository(newUserName) ?
                    getDuplPlayer(newUserName) : new Player(newUserName, 0));

            var playerId = gameSessionUtils
                    .getPlayers(newSession.id)
                    .stream().filter(p -> p.username.equals(newUserName))
                    .findFirst().get().id;

            mainCtrl.showSinglePlayer(newSession.id, playerId);
        }
    }

    /**
     * Save the username to a file so it can be retrieved after coming back to the game
     *
     * @param username - String to be saved inside the file
     */
    private void saveUsername(String username) throws IOException {
        try {
            File file = new File("username.txt");
            file.createNewFile();
            FileWriter writer = new FileWriter(file);
            writer.write(username);
            writer.close();
        } catch (IOException e) {
            throw new IOException();
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
