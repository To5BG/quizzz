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

import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.DialogPane;
import javafx.stage.Stage;
import javafx.util.Pair;

import java.util.List;
import java.nio.file.Path;

public class MainCtrl {

    // for now a field will suffice, in case more constants are needed an enum must be created
    public final static long SELECTION_ID = 1L;
    private Stage primaryStage;
    private SplashCtrl splashCtrl;
    private Scene splashScreen;
    private MultiplayerCtrl multiplayerCtrl;
    private Scene multiPlayerScreen;
    private RoomSelectionCtrl roomSelectionCtrl;
    private Scene roomSelectionScreen;
    private SingleplayerCtrl singlePlayerCtrl;
    private Scene singlePlayerScreen;
    private TimeAttackCtrl timeAttackCtrl;
    private Scene timeAttackScreen;
    private SurvivalCtrl survivalCtrl;
    private Scene survivalScreen;
    private WaitingAreaCtrl waitingAreaCtrl;
    private Scene waitingAreaScreen;
    private GamemodeCtrl gamemodeCtrl;
    private Scene gamemodeScreen;
    private LeaderBoardCtrl leaderBoardCtrl;
    private Scene leaderBoardScreen;
    private PodiumCtrl podiumCtrl;
    private Scene podiumScreen;
    private EndGameScreenCtrl endGameScreenCtrl;
    private Scene endGameScreen;
    private WebViewCtrl webViewCtrl;
    private Scene webViewScreen;
    private TutorialScreenCtrl tutorialCtrl;
    private Scene tutorialScreen;

    private List<Pair<? extends SceneCtrl, Parent>> pairs;

    /**
     * Starter method for the main controller to establish connections between scenes and store their controllers
     *
     * @param primaryStage Base stage of the application
     * @param pairs        List of all controller-scene pairs
     */
    public void initialize(Stage primaryStage, List<Pair<? extends SceneCtrl, Parent>> pairs) {
        this.primaryStage = primaryStage;
        this.pairs = pairs;

        var splash = pairs.get(0);
        this.splashCtrl = (SplashCtrl) splash.getKey();
        this.splashScreen = new Scene(splash.getValue());

        var multi = pairs.get(1);
        this.multiplayerCtrl = (MultiplayerCtrl) multi.getKey();
        this.multiPlayerScreen = new Scene(multi.getValue());

        var roomsel = pairs.get(2);
        this.roomSelectionCtrl = (RoomSelectionCtrl) roomsel.getKey();
        this.roomSelectionScreen = new Scene(roomsel.getValue());

        var single = pairs.get(3);
        this.singlePlayerCtrl = (SingleplayerCtrl) single.getKey();
        this.singlePlayerScreen = new Scene(single.getValue());

        var wait = pairs.get(4);
        this.waitingAreaCtrl = (WaitingAreaCtrl) wait.getKey();
        this.waitingAreaScreen = new Scene(wait.getValue());

        var leaderboard = pairs.get(5);
        this.leaderBoardCtrl = (LeaderBoardCtrl) leaderboard.getKey();
        this.leaderBoardScreen = new Scene(leaderboard.getValue());

        var webView = pairs.get(6);
        this.webViewCtrl = (WebViewCtrl) webView.getKey();
        this.webViewScreen = new Scene(webView.getValue());

        var tutorial = pairs.get(7);
        this.tutorialCtrl = (TutorialScreenCtrl) tutorial.getKey();
        this.tutorialScreen = new Scene(tutorial.getValue());

        var mode = pairs.get(8);
        this.gamemodeCtrl = (GamemodeCtrl) mode.getKey();
        this.gamemodeScreen = new Scene(mode.getValue());

        var timeAttack = pairs.get(9);
        this.timeAttackCtrl = (TimeAttackCtrl) timeAttack.getKey();
        this.timeAttackScreen = new Scene(timeAttack.getValue());

        var survival = pairs.get(10);
        this.survivalCtrl = (SurvivalCtrl) survival.getKey();
        this.survivalScreen = new Scene(survival.getValue());

        var podium = pairs.get(11);
        this.podiumCtrl = (PodiumCtrl) podium.getKey();
        this.podiumScreen = new Scene(podium.getValue());

        var endScreen = pairs.get(12);
        this.endGameScreenCtrl = (EndGameScreenCtrl) endScreen.getKey();
        this.endGameScreen = new Scene(endScreen.getValue());


        confirmClose();
        showSplash();
        primaryStage.show();

    }

    /**
     * Sets the current screen to the splash screen.
     */
    public void showSplash() {
        primaryStage.setTitle("Main menu");
        primaryStage.setScene(splashScreen);
        splashCtrl.retrieveSavedName();
    }

    /**
     * Sets the current screen to the multiplayer screen and adds the player to the game session DB. Loads the first
     * question. If the session is in the Play Again status, simply adds the player to the end game screen
     *
     * @param sessionId Id of session to be joined
     * @param playerId  Id of player that is about to join
     */
    public void showMultiplayer(long sessionId, long playerId) {
        primaryStage.setTitle("Multiplayer game");
        primaryStage.setScene(multiPlayerScreen);
        multiPlayerScreen.setOnKeyPressed(e -> multiplayerCtrl.keyPressed(e));
        multiplayerCtrl.setSessionId(sessionId);
        multiplayerCtrl.setPlayerId(playerId);
        multiplayerCtrl.registerForEmojiUpdates();
        multiplayerCtrl.fetchJokerStates();
        multiplayerCtrl.scanForDisconnect();
        multiplayerCtrl.scanForJokerUsage();
        multiplayerCtrl.loadQuestion();
    }

    /**
     * Sets the current screen to the room selection area.
     * Contains a scheduled task to refresh the available waiting rooms.
     *
     * @param playerId - The id of the player that's joining
     */
    public void showRoomSelection(long playerId) {
        primaryStage.setTitle("Room Selection");
        primaryStage.setScene(roomSelectionScreen);
        roomSelectionScreen.setOnKeyPressed(e -> roomSelectionCtrl.keyPressed(e));
        roomSelectionCtrl.setPlayerId(playerId);
        roomSelectionCtrl.refresh(null);
        roomSelectionCtrl.registerForUpdates();
    }

    /**
     * Sets the current screen to the waiting area and adds the player to it. Contains a
     * scheduled task to refresh the waiting area player board.
     *
     * @param playerId - new Id for the player that's about to join
     */
    public void showWaitingArea(long playerId, long waitingId) {
        primaryStage.setTitle("Waiting area");
        primaryStage.setScene(waitingAreaScreen);
        waitingAreaScreen.setOnKeyPressed(e -> waitingAreaCtrl.keyPressed(e));
        waitingAreaCtrl.setPlayerId(playerId);
        waitingAreaCtrl.setWaitingId(waitingId);
        waitingAreaCtrl.refresh(null);
        waitingAreaCtrl.registerForUpdates();
    }

    /**
     * Sets the current screen to the gamemode screen and adds a player to it.
     *
     * @param playerId The Id of player that entered the gamemodeScreen.
     */
    public void showGamemodeScreen(long playerId) {
        primaryStage.setTitle("Singleplayer gamemodes");
        primaryStage.setScene(gamemodeScreen);
        gamemodeScreen.setOnKeyPressed(e -> gamemodeCtrl.keyPressed(e));
        gamemodeCtrl.setPlayerId(playerId);
    }


    /**
     * Sets the current screen to the single player screen.
     */
    public void showDefaultSinglePlayer(long sessionId, long playerId, int questions) {
        primaryStage.setTitle("Single player game");
        primaryStage.setScene(singlePlayerScreen);
        singlePlayerScreen.setOnKeyPressed(e -> singlePlayerCtrl.keyPressed(e));
        singlePlayerCtrl.setSessionId(sessionId);
        singlePlayerCtrl.setPlayerId(playerId);
        singlePlayerCtrl.fetchJokerStates();
        singlePlayerCtrl.setGameRounds(questions);
        singlePlayerCtrl.loadQuestion();
        singlePlayerCtrl.refresh();
    }

    /**
     * Sets the current screen to the time attack screen.
     */
    public void showTimeAttack(long sessionId, long playerId, double timer) {
        primaryStage.setTitle("Time Attack");
        primaryStage.setScene(timeAttackScreen);
        timeAttackScreen.setOnKeyPressed(e -> timeAttackCtrl.keyPressed(e));
        timeAttackCtrl.setSessionId(sessionId);
        timeAttackCtrl.setPlayerId(playerId);
        timeAttackCtrl.setTimer(timer);
        timeAttackCtrl.startTimer();
        timeAttackCtrl.refresh();
    }

    /**
     * Sets the current screen to the survival screen.
     */
    public void showSurvival(long sessionId, long playerId, double lives) {
        primaryStage.setTitle("Survival Mode");
        primaryStage.setScene(survivalScreen);
        survivalScreen.setOnKeyPressed(e -> survivalCtrl.keyPressed(e));
        survivalCtrl.setSessionId(sessionId);
        survivalCtrl.setPlayerId(playerId);
        survivalCtrl.setLives(lives);
        survivalCtrl.loadQuestion();
        survivalCtrl.refresh();
    }

    /**
     * Sets the current screen to the leaderboard screen.
     */
    public void showLeaderboard() {
        primaryStage.setTitle("LeaderBoard");
        primaryStage.setScene(leaderBoardScreen);
        leaderBoardScreen.setOnKeyPressed(e -> leaderBoardCtrl.keyPressed(e));
        leaderBoardCtrl.refresh(null);
        leaderBoardCtrl.registerForUpdates();

    }

    /**
     * Sets the current screen to the WebView screen.
     */
    public void showWebView(String url) {
        primaryStage.setTitle("Edit activities");
        primaryStage.setScene(webViewScreen);
        webViewCtrl.setPage(url);
        webViewCtrl.loadPage();
    }

    /**
     * Sets the current scene to the tutorial screen
     */
    public void showTutorial() {
        tutorialCtrl.initialise();
        primaryStage.setTitle("Tutorial Screen");
        primaryStage.setScene(tutorialScreen);
        tutorialScreen.setOnKeyPressed(e -> tutorialCtrl.keyPressed(e));
    }

    /**
     * Ask the user for confirmation before closing the app
     */
    public void confirmClose() {
        primaryStage.setOnCloseRequest(evt -> {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Confirm Close");
            alert.setHeaderText("Close the program?");
            addCSS(alert);
            alert.showAndWait().filter(r -> r != ButtonType.OK).ifPresent(r -> {evt.consume();});
            if (evt.isConsumed()) return;
            for (var pair : pairs) {
                try {
                    pair.getKey().shutdown();
                } catch (Exception ignored) {
                }
            }
        });
    }

    /**
     * Add Alert.css stylesheet to the alert
     *
     * @param alert - Alert to which to add css
     */
    public void addCSS(Alert alert) {
        DialogPane dialogPane = alert.getDialogPane();
        dialogPane.getStylesheets().add(
                String.valueOf(getClass().getClassLoader().getResource(Path.of("", "CSS", "Alert.css").toString())));

    }

    /**
     * Sets the current screen to the podium screen
     *
     * @param sessionId the id of the current game session
     */
    public void showPodiumScreen(long sessionId, long playerId) {
        primaryStage.setTitle("Podium");
        primaryStage.setScene(podiumScreen);
        podiumCtrl.createPodium(sessionId);
        podiumCtrl.setPlayerId(playerId);
        podiumCtrl.setSessionId(sessionId);
    }

    /**
     * Sets the current screen to the end of game screen
     *
     * @param sessionId The id of the current game session
     */
    public void showEndGameScreen(long sessionId, long playerId) {
        primaryStage.setTitle("End of game");
        primaryStage.setScene(endGameScreen);
        endGameScreenCtrl.setPlayerId(playerId);
        endGameScreenCtrl.setSessionId(sessionId);
        endGameScreenCtrl.showEndScreen();
    }
}