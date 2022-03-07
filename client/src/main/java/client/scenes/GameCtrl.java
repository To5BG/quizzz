package client.scenes;

import client.utils.ServerUtils;
import commons.Answer;
import commons.Evaluation;
import commons.GameSession;
import commons.Question;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.RadioButton;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.StackPane;

import java.util.*;

public abstract class GameCtrl {

    protected final int GAME_ROUNDS = 5;
    protected final int GAME_ROUND_TIME = 10;
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

    protected ServerUtils server;
    protected MainCtrl mainCtrl;

    protected List<RadioButton> multiChoiceAnswers;
    protected long sessionId;
    protected long playerId;
    protected Question currentQuestion;
    protected int points = 0;
    protected int rounds = 0;
    protected Thread timerThread;

    private boolean doublePointsJoker;

    //@Inject
    public GameCtrl(ServerUtils server, MainCtrl mainCtrl) {
        this.server = server;
        this.mainCtrl = mainCtrl;
        this.multiChoiceAnswers = new ArrayList<RadioButton>();
        this.doublePointsJoker = false;
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
     * Removes player from session, along with the singleplayer session. Also called if controller is closed forcibly
     */
    public void shutdown() {
        if (sessionId != 0)  {
            server.removePlayer(sessionId, playerId);
            setPlayerId(0);
        }
        if (server.getPlayers(sessionId).size() == 0) {
            server.removeSession(sessionId);
            this.timerThread.interrupt();
            setSessionId(0);
        }
    }

    /**
     * Reverts the player to the splash screen and remove him from the current game session.
     */
    public void back() {
        shutdown();
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
     * Disable button so the player can not interact with it
     *
     * @param button  - Button to be disabled
     * @param disable - boolean value whether the button should be disabled or enabled
     */
    public void disableButton(Button button, boolean disable) {
        button.setDisable(disable);
    }

    /**
     * Remove One Answer Joker
     * When this joker is used it removes one incorrect answer from the answers list for the player that used it
     */
    public void removeOneAnswer() {
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
            default -> disableButton(removeOneButton, false);
        }


    }

    /**
     * Decrease Time Joker
     * When this joker is used, it decreases the time by a set percentage
     * This joker can not be used in single-player
     */
    public void decreaseTime() {
        disableButton(decreaseTimeButton, true);
        //TODO Add functionality to button when multiplayer is functional
    }

    /**
     * Double Points Joker
     * When this joker is used, it doubles the points gained for the question when it was used.
     */
    public void doublePoints() {
        disableButton(doublePointsButton, true);
        switchStatusOfDoublePoints();
    }

    /**
     * Check if the doublePointsJoker is active for this question
     *
     * @return true if the joker is active
     */
    private boolean doublePointsIsActive() {
        return doublePointsJoker;
    }

    /**
     * Switch the doublePointsJoker status from true to false and from false to true
     */
    private void switchStatusOfDoublePoints() {
        doublePointsJoker = !doublePointsJoker;
    }

}
