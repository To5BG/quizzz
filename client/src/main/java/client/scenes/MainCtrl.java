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
import javafx.stage.Stage;
import javafx.util.Pair;

import java.util.Timer;
import java.util.TimerTask;

public class MainCtrl {

    private Stage primaryStage;

    private SplashCtrl splashCtrl;
    private Scene splashScreen;

    private MultiplayerCtrl multiplayerCtrl;
    private Scene multiPlayerScreen;

    private WaitingAreaCtrl waitingAreaCtrl;
    private Scene waitingAreaScreen;

    /**
     * Starter method for the main controller to establish connections between scenes and store their controllers
     *
     * @param primaryStage store base stage of the application
     * @param splash       Controller and Scene pair for the splash screen of the application
     * @param multi        Controller and Scene pair for the multiplayer screen of the application
     */
    public void initialize(Stage primaryStage, Pair<SplashCtrl, Parent> splash,
                           Pair<MultiplayerCtrl, Parent> multi,
                           Pair<WaitingAreaCtrl, Parent> wait) {
        this.primaryStage = primaryStage;

        this.splashCtrl = splash.getKey();
        this.splashScreen = new Scene(splash.getValue());

        this.multiplayerCtrl = multi.getKey();
        this.multiPlayerScreen = new Scene(multi.getValue());

        this.waitingAreaCtrl = wait.getKey();
        this.waitingAreaScreen = new Scene(wait.getValue());

        showSplash();
        primaryStage.show();
    }

    /**
     * Sets the current screen to the splash screen.
     */
    public void showSplash() {
        primaryStage.setTitle("Main menu");
        primaryStage.setScene(splashScreen);
    }

    /**
     * Sets the current screen to the multiplayer screen and adds the player to the game session DB. Contains a
     * scheduled task to refresh the multiplayer player board.
     * @param sessionId Id of session to be joined
     * @param playerId Id of player that is about to join
     */
    public void enterMultiplayerGame(long sessionId, long playerId) {
        primaryStage.setTitle("Multiplayer game");
        primaryStage.setScene(multiPlayerScreen);
        multiPlayerScreen.setOnKeyPressed(e -> multiplayerCtrl.keyPressed(e));
        multiplayerCtrl.setSessionId(sessionId);
        multiplayerCtrl.setPlayerId(playerId);

        new Timer().scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                try {
                    multiplayerCtrl.refresh();
                } catch (Exception e) {
                    cancel();
                }
            }

            @Override
            public boolean cancel() {
                return super.cancel();
            }
        }, 0, 500);
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
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            if (!waitingAreaCtrl.refresh()) cancel();
                        } catch (Exception e) {
                            cancel();
                        }
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
     * Sets the current screen to the singleplayer screen.
     */
    public void showSingleplayer() {
    }

    /**
     * Sets the current room to the leaderboard screen.
     */
    public void showLeaderboard() {
    }
}