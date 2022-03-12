package client.scenes;

import client.utils.ServerUtils;
import commons.*;
import jakarta.ws.rs.BadRequestException;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.StackPane;

import java.util.*;

public abstract class GameCtrl implements Initializable {

    protected final int GAME_ROUNDS = 5;
    protected final int GAME_ROUND_TIME = 10;
    protected final int MIDGAME_BREAK_TIME = 6;
    protected final int TIMER_UPDATE_INTERVAL_MS = 50;
    protected final int GAME_ROUND_DELAY = 2;

    @FXML
    protected StackPane answerArea;

    @FXML
    protected Label questionPrompt;

    @FXML
    protected Label pointsLabel;

    @FXML
    protected ProgressBar timeProgress;

    @FXML
    protected Button submitButton;

    @FXML
    protected Button removeOneButton;

    @FXML
    protected Button decreaseTimeButton;

    @FXML
    protected Button doublePointsButton;

    @FXML
    protected TableView<Player> leaderboard;

    @FXML
    protected TableColumn<Player, Integer> colRank;

    @FXML
    protected TableColumn<Player, String> colUserName;

    @FXML
    protected TableColumn<Player, Integer> colPoints;

    protected ServerUtils server;
    protected MainCtrl mainCtrl;

    protected List<RadioButton> multiChoiceAnswers;
    protected long sessionId;
    protected long playerId;
    protected Question currentQuestion;
    protected int points = 0;
    protected int bestScore = 0;
    protected int rounds = 0;
    protected Thread timerThread;

    protected boolean doublePointsJoker;
    protected boolean doublePointsActive;
    protected boolean decreaseTimeJoker;
    protected boolean removeOneJoker;


    public GameCtrl(ServerUtils server, MainCtrl mainCtrl) {
        this.server = server;
        this.mainCtrl = mainCtrl;
        this.multiChoiceAnswers = new ArrayList<RadioButton>();

        doublePointsJoker = true;
        doublePointsActive = false;
        decreaseTimeJoker = true;
        removeOneJoker = true;

        // Set to defaults
        this.sessionId = 0L;
        this.playerId = 0L;
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

    /**
     * Setter for bestScore.
     */
    public void setBestScore() {
        this.bestScore = server.getPlayerById(playerId).bestScore;
    }

    /**
     * Load general question information
     *
     * @param q
     */
    protected void renderGeneralInformation(Question q) {
        this.questionPrompt.setText(q.prompt);
        // TODO load image
    }

    /**
     * Switch method to render answer options for a given question
     *
     * @param q Question from which to take the possible answers
     */
    protected void renderAnswerFields(Question q) {
        switch (q.type) {
            case MULTIPLE_CHOICE:
                renderMultipleChoiceQuestion(q);
                if(removeOneJoker) {
                    disableButton(removeOneButton, false);
                }
                break;
            default:
                throw new UnsupportedOperationException("Currently only multiple choice questions can be rendered");
        }
    }

    /**
     * Render question of the multiple choice question variant
     *
     * @param q
     */
    protected void renderMultipleChoiceQuestion(Question q) {
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
     * Switch method to render correct answers based on the question type
     *
     * @param eval Evaluation containing the true answers
     */
    protected void renderCorrectAnswer(Evaluation eval) {
        switch (eval.type) {
            case MULTIPLE_CHOICE:
                renderMultipleChoiceAnswers(eval.correctAnswers);
                break;
            default:
                throw new UnsupportedOperationException("Currently only multiple choice answers can be rendered");
        }
    }

    /**
     * Render answers of the multiple choice question variant
     *
     * @param correctIndices Indexes of the correct answer(s)
     */
    protected void renderMultipleChoiceAnswers(List<Integer> correctIndices) {
        for (int i = 0; i < multiChoiceAnswers.size(); ++i) {
            if (correctIndices.contains(i)) {
                multiChoiceAnswers.get(i).setStyle("-fx-background-color: green");
            } else {
                multiChoiceAnswers.get(i).setDisable(true);
            }
        }
    }

    /**
     * Loads a question and updates the timer bar
     */
    public void loadQuestion() {
        disableButton(removeOneButton, true);
        disableButton(doublePointsButton, true);
        disableButton(decreaseTimeButton, true);

        Question q = this.server.fetchOneQuestion(this.sessionId);
        this.currentQuestion = q;
        renderGeneralInformation(q);
        renderAnswerFields(q);
        disableButton(submitButton, false);

        Task roundTimer = new Task() {
            @Override
            public Object call() {
                long refreshCounter = 0;
                long gameRoundMs = GAME_ROUND_TIME * 1000;
                long timeElapsed = 0;
                while (timeElapsed < gameRoundMs) {
                    long booster = getTimeJokers() + 1;
                    updateProgress(gameRoundMs - timeElapsed, gameRoundMs);
                    refreshCounter += booster;
                    try {
                        Thread.sleep(TIMER_UPDATE_INTERVAL_MS);
                        timeElapsed = refreshCounter * TIMER_UPDATE_INTERVAL_MS;
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
     * Removes player from session, along with the singleplayer session. Also called if controller is closed forcibly
     */
    public void shutdown() {
        if (this.timerThread != null && this.timerThread.isAlive()) this.timerThread.interrupt();
        if (sessionId != 0) {
            server.updateScore(playerId, 0, false);
            server.addPlayerAnswer(sessionId, playerId, new Answer(Question.QuestionType.MULTIPLE_CHOICE));
            server.removePlayer(sessionId, playerId);
            setPlayerId(0);
        }
        if (server.getPlayers(sessionId).size() == 0) {
            server.removeSession(sessionId);
            setSessionId(0);
        }
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
        this.rounds = 0;
        this.currentQuestion = null;

        //re-enable jokers
        doublePointsJoker = true;
        doublePointsActive = false;
        decreaseTimeJoker = true;
        removeOneJoker = true;

        disableButton(submitButton, true);
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
     * Updates the point counter in client side, and then updates database entry
     *
     * @param eval Evaluation of received answers
     */
    public void updatePoints(Evaluation eval) {
        if(doublePointsActive) {
            points = points + 2 * eval.points;
            switchStatusOfDoublePoints();
        } else {
            points += eval.points;
        }
        renderPoints();
        server.updateScore(playerId, points, false);
    }

    /**
     * Called when player points are rendered or updated
     */
    public void renderPoints() {
        pointsLabel.setText(String.format("Points: %d", this.points));
    }

    /**
     * Submit an answer to the server
     */
    public void submitAnswer() {
        /* RadioButton rb = new RadioButton("Answer option #1");
        answerArea.getChildren().add(rb); */
        if (this.timerThread != null && this.timerThread.isAlive()) this.timerThread.interrupt();
        disableButton(submitButton, true);
        disableButton(removeOneButton, true);
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
    }

    /**
     * Gets the user's answer, starts the evaluation and loads a new question or ends the game.
     */
    public void startEvaluation() {

        Answer ans = server.getPlayerAnswer(sessionId, playerId);
        Evaluation eval = server.submitAnswer(sessionId, ans);

        disableButton(removeOneButton, true);
        disableButton(decreaseTimeButton, true);
        disableButton(doublePointsButton, true);

        updatePoints(eval);
        renderCorrectAnswer(eval);

        // TODO disable button while waiting
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                Platform.runLater(() -> {
                    rounds++;
                    resetTimeJokers();
                    if (rounds == GAME_ROUNDS) {
                        // TODO display leaderboard things here
                        if (points > bestScore) server.updateScore(playerId, points, true);
                        back();
                    } else if (rounds == GAME_ROUNDS / 2 &&
                            server.getSession(sessionId).sessionType == GameSession.SessionType.MULTIPLAYER) {
                        displayMidGameScreen();
                    } else {
                        try {
                            GameSession session = server.toggleReady(sessionId, false);
                            if (session.playersReady == 0) {
                                server.updateStatus(session, GameSession.SessionStatus.ONGOING);
                            }
                            loadQuestion();
                        } catch (BadRequestException e) {
                            System.out.println("takingover");
                        }
                    }
                });
            }
        }, GAME_ROUND_DELAY * 1000);
    }

    /**
     * Display mid-game leaderboard
     */
    public void displayMidGameScreen() {
        var players = server.getPlayers(sessionId);
        var data = FXCollections.observableList(players);
        leaderboard.setItems(data);
        answerArea.setOpacity(0);
        submitButton.setOpacity(0);
        removeOneButton.setOpacity(0);
        doublePointsButton.setOpacity(0);
        decreaseTimeButton.setOpacity(0);
        questionPrompt.setOpacity(0);
        leaderboard.setOpacity(1);

        Task roundTimer = new Task() {
            @Override
            public Object call() {
                long refreshCounter = 0;
                long gameRoundMs = MIDGAME_BREAK_TIME * 1000;
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
                return null;
            }
        };
        timeProgress.progressProperty().bind(roundTimer.progressProperty());
        this.timerThread = new Thread(roundTimer);
        this.timerThread.start();

        GameSession session = server.toggleReady(sessionId, false);
        if (session.playersReady == 0) {
            server.updateStatus(session, GameSession.SessionStatus.ONGOING);
        }
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                Platform.runLater(() -> {
                    leaderboard.setOpacity(0);
                    answerArea.setOpacity(1);
                    questionPrompt.setOpacity(1);
                    submitButton.setOpacity(1);
                    removeOneButton.setOpacity(1);
                    doublePointsButton.setOpacity(1);
                    decreaseTimeButton.setOpacity(1);
                    loadQuestion();
                });
            }
        }, MIDGAME_BREAK_TIME * 1000);
    }

    /**
     * Disable button so the player can not interact with it
     *
     * @param button  - Button to be disabled
     * @param disable - boolean value whether the button should be disabled or enabled
     */
    public void disableButton(Button button, boolean disable) {
        if(disable) button.setOpacity(0.5);
        if(!disable) button.setOpacity(1);
        button.setDisable(disable);
    }

    /**
     * Remove One Answer Joker
     * When this joker is used it removes one incorrect answer from the answers list for the player that used it
     */
    public void removeOneAnswer() {
        removeOneJoker = false;
        disableButton(removeOneButton, true);

        switch (currentQuestion.type) {
            case MULTIPLE_CHOICE -> {
                List<Integer> incorrectAnswers = new ArrayList<>();
                List<Integer> correctAnswers = server.getCorrectAnswers(sessionId);
                for (int i = 0; i < multiChoiceAnswers.size(); ++i) {
                    if (!correctAnswers.contains(i)) {
                        incorrectAnswers.add(i);
                    }
                }
                int randomIndex = new Random().nextInt(incorrectAnswers.size());
                multiChoiceAnswers.get(incorrectAnswers.get(randomIndex)).setDisable(true);
            }
            default -> {
                disableButton(removeOneButton, false);
            }
        }


    }

    /**
     * Get number of time Jokers for the current session
     * @return int representing number of time jokers
     */
    public int getTimeJokers() {
        return server.getSession(sessionId).getTimeJokers();
    }

    /**
     * Reset the number of time Jokers for the current session to 0
     */
    public void resetTimeJokers() {
        if(server.getSession(sessionId).getTimeJokers() != 0) {
            server.updateTimeJokers(sessionId, 0);
        }
    }
    /**
     * Decrease Time Joker
     * When this joker is used, it decreases the time by a set percentage
     * This joker can not be used in single-player
     */
    public void decreaseTime() {
        decreaseTimeJoker = false;
        disableButton(decreaseTimeButton, true);
        //TODO Add functionality to button when multiplayer is functional
        server.updateTimeJokers(sessionId, getTimeJokers() + 1);
    }

    /**
     * Double Points Joker
     * When this joker is used, it doubles the points gained for the question when it was used.
     */
    public void doublePoints() {
        doublePointsJoker = false;
        disableButton(doublePointsButton, true);
        switchStatusOfDoublePoints();
    }

    /**
     * Switch the doublePointsJoker status from true to false and from false to true
     */
    private void switchStatusOfDoublePoints() {
        doublePointsActive = !doublePointsActive;
    }

}
