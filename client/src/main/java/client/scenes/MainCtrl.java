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

import javafx.application.Platform;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.stage.Stage;
import javafx.util.Pair;

import java.util.Timer;
import java.util.TimerTask;

public class MainCtrl {

    // for now a field will suffice, in case more constants are needed an enum must be created
    public final static long WAITING_AREA_ID = 1L;
    private Stage primaryStage;
    private SplashCtrl splashCtrl;
    private Scene splashScreen;
    private MultiplayerCtrl multiplayerCtrl;
    private Scene multiPlayerScreen;
    private SingleplayerCtrl singlePlayerCtrl;
    private Scene singlePlayerScreen;
    private WaitingAreaCtrl waitingAreaCtrl;
    private Scene waitingAreaScreen;
    private LeaderBoardCtrl leaderBoardCtrl;
    private Scene leaderBoardScreen;

    /**
     * Starter method for the main controller to establish connections between scenes and store their controllers
     *
     * @param primaryStage store base stage of the application
     * @param splash       Controller and Scene pair for the splash screen of the application
     * @param multi        Controller and Scene pair for the multiplayer screen of the application
     * @param leaderboard  Controller and Scene pair for the leaderboard screen of the application
     */
    public void initialize(Stage primaryStage, Pair<SplashCtrl, Parent> splash,
                           Pair<MultiplayerCtrl, Parent> multi,
                           Pair<WaitingAreaCtrl, Parent> wait,
                           Pair<SingleplayerCtrl, Parent> single,
                           Pair<LeaderBoardCtrl, Parent> leaderboard) {
        this.primaryStage = primaryStage;

        this.splashCtrl = splash.getKey();
        this.splashScreen = new Scene(splash.getValue());

        this.multiplayerCtrl = multi.getKey();
        this.multiPlayerScreen = new Scene(multi.getValue());

        this.waitingAreaCtrl = wait.getKey();
        this.waitingAreaScreen = new Scene(wait.getValue());

        this.singlePlayerCtrl = single.getKey();
        this.singlePlayerScreen = new Scene(single.getValue());

        this.leaderBoardCtrl = leaderboard.getKey();
        this.leaderBoardScreen = new Scene(leaderboard.getValue());

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
     * question.
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
        multiplayerCtrl.loadQuestion();
        multiplayerCtrl.scanForDisconnect();
        multiplayerCtrl.scanForJokerUsage();
    }

    /**
     * Sets the current screen to the waiting area and adds the player to it. Contains a
     * scheduled task to refresh the waiting area player board.
     *
     * @param playerId Id of player that's about to join
     */
    public void showWaitingArea(long playerId) {
        primaryStage.setTitle("Waiting area");
        primaryStage.setScene(waitingAreaScreen);
        waitingAreaScreen.setOnKeyPressed(e -> waitingAreaCtrl.keyPressed(e));
        waitingAreaCtrl.setPlayerId(playerId);

        new Timer().scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                Platform.runLater(() -> {
                    try {
                        if (!waitingAreaCtrl.refresh()) cancel();
                    } catch (Exception e) {
                        cancel();
                    }
                });
            }

            @Override
            public boolean cancel() {
                return super.cancel();
            }
        }, 0, 500);
    }


    /**
     * Sets the current screen to the single player screen.
     */
    public void showSinglePlayer(long sessionId, long playerId) {
        primaryStage.setTitle("Singe player game");
        primaryStage.setScene(singlePlayerScreen);
        singlePlayerScreen.setOnKeyPressed(e -> singlePlayerCtrl.keyPressed(e));
        singlePlayerCtrl.setSessionId(sessionId);
        singlePlayerCtrl.setPlayerId(playerId);
        singlePlayerCtrl.loadQuestion();
        singlePlayerCtrl.refresh();
    }

    /**
     * Sets the current screen to the leaderboard screen.
     */
    public void showLeaderboard() {
        primaryStage.setTitle("LeaderBoard");
        primaryStage.setScene(leaderBoardScreen);
        leaderBoardCtrl.refreshSingle();
        leaderBoardScreen.setOnKeyPressed(e -> leaderBoardCtrl.keyPressed(e));

    }

    /**
     * Ask the user for confirmation before closing the app
     */
    public void confirmClose() {
        primaryStage.setOnCloseRequest(evt -> {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Confirm Close");
            alert.setHeaderText("Close the program?");
            alert.showAndWait().filter(r -> r != ButtonType.OK).ifPresent(r -> evt.consume());
        });
    }
}