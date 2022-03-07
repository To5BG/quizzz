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
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.StackPane;

import java.util.*;

public class MultiplayerCtrl {

    private final int GAME_ROUNDS = 5;
    private final int GAME_ROUND_TIME = 10;
    private final int TIMER_UPDATE_INTERVAL_MS = 50;
    private final int GAME_ROUND_DELAY = 2;

    @FXML
    private StackPane answerArea;

    @FXML
    private Label questionPrompt;

    @FXML
    private Label pointsLabel;

    @FXML
    private ProgressBar timeProgress;

    @FXML
    private Button submitButton;

    private final ServerUtils server;
    private final MainCtrl mainCtrl;
    private long sessionId;
    private long playerId;
    private List<RadioButton> multiChoiceAnswers;
    private Question currentQuestion;
    private int points = 0;
    private int rounds = 0;
    private Thread timerThread;


    @Inject
    public MultiplayerCtrl(ServerUtils server, MainCtrl mainCtrl) {
        this.mainCtrl = mainCtrl;
        this.server = server;
        this.multiChoiceAnswers = new ArrayList<RadioButton>();
        // Set to defaults
        this.sessionId = 0L;
        this.playerId = 0L;
    }

    /**
     * Removes player from the current session and also the session itself if empty.
     * Also called if controller is closed forcibly
     */
    public void shutdown() {
        if (sessionId != 0) server.removePlayer(sessionId, playerId);
        if (server.getPlayers(sessionId).size() == 0) server.removeSession(sessionId);
    }

    /**
     * Reverts the player to the splash screen and remove him from the current game session.
     */
    public void back() {
        shutdown();
        this.questionPrompt.setText("[Question]");
        this.answerArea.getChildren().clear();
        this.pointsLabel.setText("Points: 0");
        this.multiChoiceAnswers.clear();
        this.points = 0;
        this.currentQuestion = null;
        this.submitButton.setDisable(true);
        mainCtrl.showSplash();
        sessionId = playerId = 0L;
        mainCtrl.showSplash();
    }

    /**
     * Switch method that maps keyboard key presses to functions.
     *
     * @param e KeyEvent to be switched
     */
    public void keyPressed(KeyEvent e) {
        switch (e.getCode()) {
            case ESCAPE -> back();
        }
    }

    /**
     * Refreshes the multiplayer player board to check whether the evaluation can start.
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
                                startEvaluation();
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
     * Sets the answer buttons for a multiple choice question.
     *
     * @param q The multiple choice question.
     */
    private void renderMultipleChoiceQuestion(Question q) {
        double yPosition = 0.0;
        multiChoiceAnswers.clear();
        answerArea.getChildren().clear();
        for (String opt : q.answerOptions) {
            RadioButton choice = new RadioButton(opt);
            choice.setTranslateY(yPosition);
            yPosition += 30;
            multiChoiceAnswers.add(choice);
            answerArea.getChildren().add(choice);
        }
    }

    /**
     * Assigns a green colour to the correct answer field(s).
     *
     * @param correctIndices The correct answer(s).
     */
    private void renderMultipleChoiceAnswers(List<Integer> correctIndices) {
        for (int i = 0; i < multiChoiceAnswers.size(); ++i) {
            if (correctIndices.contains(i)) {
                multiChoiceAnswers.get(i).setStyle("-fx-background-color: green");
            }
        }
    }

    /**
     * Renders the question prompt with the current question.
     *
     * @param q The current question.
     */
    private void renderGeneralInformation(Question q) {
        this.questionPrompt.setText(q.prompt);
        // TODO load image
    }

    /**
     * Envokes the correct method that renders the answer fields for the current question type.
     *
     * @param q The current question.
     */
    private void renderAnswerFields(Question q) {
        switch (q.type) {
            case MULTIPLE_CHOICE:
                renderMultipleChoiceQuestion(q);
                break;
            default:
                throw new UnsupportedOperationException("Currently only multiple choice questions can be rendered");
        }
    }

    /**
     * Fetches a question, loads the question and starts the progressbar.
     */
    public void loadQuestion() {
        Question q = this.server.fetchOneQuestion(this.sessionId);
        this.currentQuestion = q;
        renderGeneralInformation(q);
        renderAnswerFields(q);
        this.submitButton.setDisable(false);

        Task roundTimer = new Task() {
            @Override
            public Object call() {
                long refreshCounter = 0;
                long gameRoundMs = GAME_ROUND_TIME * 1000;
                while (refreshCounter * TIMER_UPDATE_INTERVAL_MS < gameRoundMs) {
                    updateProgress(gameRoundMs - refreshCounter * TIMER_UPDATE_INTERVAL_MS, gameRoundMs);
                    ++refreshCounter;
                    try {
                        Thread.sleep(TIMER_UPDATE_INTERVAL_MS);
                    } catch (InterruptedException e) {
                        updateProgress(0, 1);
                        return null;
                    }
                }
                updateProgress(0, 1);
                Platform.runLater(() -> submitAnswer());
                return null;
            }
        };

        timeProgress.progressProperty().bind(roundTimer.progressProperty());
        this.timerThread = new Thread(roundTimer);
        this.timerThread.start();
    }

    /**
     * Envokes the correct method that shows the correct answer for the current question type.
     *
     * @param eval The evaluation of the current question.
     */
    private void renderCorrectAnswer(Evaluation eval) {
        switch (eval.type) {
            case MULTIPLE_CHOICE:
                renderMultipleChoiceAnswers(eval.correctAnswers);
                break;
            default:
                throw new UnsupportedOperationException("Currently only multiple choice answers can be rendered");
        }
    }

    /**
     * Renders the user's score.
     */
    public void renderPoints() {
        pointsLabel.setText(String.format("Points: %d", this.points));
    }

    /**
     * Interrupts the timer, disables the submit button, sends the user's answer for evaluation and pauses the game
     * until everyone has answered or the timer has terminated.
     */
    public void submitAnswer() {
        /* RadioButton rb = new RadioButton("Answer option #1");
        answerArea.getChildren().add(rb); */
        if (this.timerThread != null && this.timerThread.isAlive()) this.timerThread.interrupt();
        this.submitButton.setDisable(true);
        server.toggleReady(sessionId, true);

        Answer ans = new Answer(currentQuestion.type);
        for (int i = 0; i < multiChoiceAnswers.size(); ++i) {
            if (multiChoiceAnswers.get(i).isSelected()) {
                ans.addAnswer(i);
            }
        }

        server.addPlayerAnswer(sessionId, playerId, ans);
        var session = server.getSession(sessionId);
        if (session.playersReady == session.players.size()) {
            server.updateStatus(session, GameSession.SessionStatus.PAUSED);
        }
        refresh();
    }

    /**
     * Gets the user's answer, starts the evaluation and loads a new question or ends the game.
     */
    public void startEvaluation() {

        Answer ans = server.getPlayerAnswer(sessionId, playerId);
        Evaluation eval = server.submitAnswer(sessionId, ans);
        points += eval.points;

        renderPoints();
        renderCorrectAnswer(eval);

        // TODO disable button while waiting
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                Platform.runLater(() -> {
                    if (++rounds == GAME_ROUNDS) {
                        // TODO display leaderboard things here
                        back();
                    } else {
                        GameSession session = server.toggleReady(sessionId, false);
                        if (session.playersReady == 0) {
                            server.updateStatus(session, GameSession.SessionStatus.ONGOING);
                        }
                        loadQuestion();
                    }
                });
            }
        }, GAME_ROUND_DELAY * 1000);
    }

    /**
     * Setter for sessionId.
     *
     * @param sessionId
     */
    public void setSessionId(long sessionId) {
        this.sessionId = sessionId;
    }

    /**
     * Setter for playerId.
     *
     * @param playerId
     */
    public void setPlayerId(long playerId) {
        this.playerId = playerId;
    }
}