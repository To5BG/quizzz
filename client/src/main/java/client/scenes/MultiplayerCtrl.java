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
import commons.*;
import javafx.application.Platform;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.util.Callback;

import java.net.URL;
import java.util.ResourceBundle;
import java.util.Timer;
import java.util.TimerTask;

public class MultiplayerCtrl extends GameCtrl {


    @Inject
    public MultiplayerCtrl(ServerUtils server, MainCtrl mainCtrl) {
        super(server, mainCtrl);
    }

    private boolean playingAgain;
    private int WAITING_SKIP = 0;

    @FXML
    private Button backButton;

    @FXML
    private Button leaveButton;

    @FXML
    private Button playAgain;

    @FXML
    private Label status;

    /**
     * {@inheritDoc}
     */
    public void initialize(URL url, ResourceBundle res) {
        colUserName.setCellValueFactory(p -> new SimpleStringProperty(p.getValue().username));
        colPoints.setCellValueFactory(p -> new SimpleIntegerProperty(p.getValue().currentPoints).asObject());

        colRank.setCellFactory(new Callback<>() {
            @Override
            public TableCell<Player, Integer> call(TableColumn<Player, Integer> param) {
                return new TableCell<>() {
                    @Override
                    protected void updateItem(Integer item, boolean empty) {
                        super.updateItem(item, empty);
                        if (!empty) setText(this.getTableRow().getIndex() + 1 + "");
                        else setText("");
                    }
                };
            }
        });

        leaveButton.setOpacity(0);
        backButton.setOpacity(1);
        playAgain.setOpacity(0);
        status.setOpacity(0);
    }

    /**
     * Refreshes the multiplayer player board to check whether the evaluation can start or refreshes the board to check
     * how many players want to play again.
     */
    public void refresh() {
        Timer t = new Timer();
        t.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            if (server.getSession(sessionId).sessionStatus
                                    == GameSession.SessionStatus.PAUSED) {
                                startEvaluation(bestMultiScore);
                                cancel();
                            }
                            if (server.getSession(sessionId).sessionStatus
                                    == GameSession.SessionStatus.PLAY_AGAIN) {
                                if (server.getSession(sessionId).players.size() ==
                                        server.getSession(sessionId).playersReady) {
                                    WAITING_SKIP = 4;
                                } else {
                                    WAITING_SKIP = 0;
                                }
                                status.setText(server.getSession(sessionId).playersReady + " / " +
                                        server.getSession(sessionId).players.size() + " players want to play again");
                            }
                            if (server.getSession(sessionId).sessionStatus
                                    == GameSession.SessionStatus.TRANSFERRING) {
                                cancel();
                            }
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
        }, 0, 100);
    }


    /**
     * Interrupts the timer, disables the submit button, sends the user's answer for evaluation and pauses the game
     * until everyone has answered or the timer has terminated.
     */
    public void submitAnswer(boolean initiatedByTimer) {
        super.submitAnswer(initiatedByTimer);
        if (!initiatedByTimer && this.evaluation == null) return;

        //enable jokers that can be used after submitting an answer
        if(decreaseTimeJoker) {
            disableButton(decreaseTimeButton, false);
        }
        if(doublePointsJoker) {
            disableButton(doublePointsButton, false);
        }

        refresh();
    }

    @Override
    public void updateScore(long playerId, int points, boolean isBestScore) {
        server.updateMultiScore(playerId, points, isBestScore);
    }

    /**
     * Method that calls the parent class' back method when the endgame back button is pressed and calls reset.
     */
    public void leaveGame() {
        if (playAgain.getText().equals("Don't play again")) playAgain();
        reset();
        super.back();
    }

    /**
     * Reset method that resets multiplayer only attributes.
     */
    @Override
    public void reset() {
        playAgain.setText("Play again");
        playAgain.setOpacity(0);
        leaveButton.setOpacity(0);
        leaveButton.setDisable(true);
        backButton.setOpacity(1);
        backButton.setDisable(false);
        status.setText("[Status]");
        status.setOpacity(0);
        setPlayingAgain(false);
        WAITING_SKIP = 0;
        super.reset();
    }

    /**
     * Toggles between want to play again and don't want to play again, modifying playAgain button and stores whether
     * the player wants to play again.
     */
    public void playAgain() {
        switch (playAgain.getText()) {
            case "Play again" -> {
                playAgain.setText("Don't play again");
                questionCount.setText("Waiting for game to start...");
                server.toggleReady(sessionId, true);
                setPlayingAgain(true);
            }
            case "Don't play again" -> {
                playAgain.setText("Play again");
                questionCount.setText("End of game! Play again or go back to main.");
                server.toggleReady(sessionId, false);
                setPlayingAgain(false);
            }
        }
    }

    /**
     * Show leaderboard at the end of the game and reveals the back button as well as the playAgain button. Starts timer
     * and after 20 seconds a new game starts if enough players want to play again.
     */
    @Override
    public void showEndScreen() {
        displayLeaderboard();
        backButton.setOpacity(0);
        backButton.setDisable(true);
        leaveButton.setOpacity(1);
        leaveButton.setDisable(false);
        playAgain.setOpacity(1);
        status.setOpacity(1);
        status.setText("");
        WAITING_SKIP = 0;
        questionCount.setText("End of game! Play again or go back to main.");

        Task roundTimer = new Task() {
            @Override
            public Object call() {
                long refreshCounter = 0;
                long waitingTime = 60000L;
                while (refreshCounter * TIMER_UPDATE_INTERVAL_MS < waitingTime) {
                    updateProgress(waitingTime - refreshCounter * TIMER_UPDATE_INTERVAL_MS, waitingTime);
                    refreshCounter += WAITING_SKIP + 1;
                    try {
                        Thread.sleep(TIMER_UPDATE_INTERVAL_MS);
                    } catch (InterruptedException e) {
                        updateProgress(0, 1);
                        return null;
                    }
                }
                updateProgress(0, 1);
                server.updateStatus(server.getSession(sessionId), GameSession.SessionStatus.TRANSFERRING);
                Platform.runLater(() -> {
                    if (!(isPlayingAgain())) {
                        leaveGame();
                    }
                    else {
                        startGame();
                    }
                });
                return null;
            }
        };
        timeProgress.progressProperty().bind(roundTimer.progressProperty());
        this.timerThread = new Thread(roundTimer);
        this.timerThread.start();

        GameSession session = server.toggleReady(sessionId, false);
        server.updateStatus(session, GameSession.SessionStatus.PLAY_AGAIN);
        refresh();
    }

    /**
     * Checks whether there are enough players in the session after the clients had time to remove the players that
     * quit.
     */
    public void startGame() {
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                Platform.runLater(() -> {
                    if (server.getPlayers(sessionId).size() >= 2 && isPlayingAgain()) {
                        GameSession session = server.toggleReady(sessionId, false);
                        if (session.playersReady == 0) {
                            server.updateStatus(session, GameSession.SessionStatus.ONGOING);
                            server.resetQuestionCounter(sessionId);
                        }
                        reset();
                        loadQuestion();
                    }
                    else {
                        leaveGame();
                    }
                });
            }
        }, 1000);
    }

    /**
     * Setter for playingAgain field
     *
     * @param playingAgain parameter that shows if a player wants to play again.
     */
    public void setPlayingAgain(boolean playingAgain) {
        this.playingAgain = playingAgain;
    }

    /**
     * Getter for playingAgain field.
     *
     * @return whether the player wants to play again.
     */
    public boolean isPlayingAgain() {
        return playingAgain;
    }
}