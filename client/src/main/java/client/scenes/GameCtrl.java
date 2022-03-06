package client.scenes;

import client.utils.ServerUtils;
import com.google.inject.Inject;
import commons.Answer;
import commons.Evaluation;
import commons.Question;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.RadioButton;
import javafx.scene.layout.StackPane;

import java.util.*;

public class GameCtrl {

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

    @FXML
    private Button removeOneButton;

    @FXML
    private Button decreaseTimeButton;

    @FXML
    private Button doublePointsButton;

    private ServerUtils api;
    private MainCtrl main;

    private List<RadioButton> multiChoiceAnswers;
    private long sessionId;
    private long playerId;
    private Question currentQuestion;
    private int points = 0;
    private int rounds = 0;
    private Thread timerThread;
    private boolean doublePointsJoker;

    @Inject
    public GameCtrl(ServerUtils api, MainCtrl main) {
        this.api = api;
        this.main = main;
        this.multiChoiceAnswers = new ArrayList<RadioButton>();
        this.doublePointsJoker = false;
    }

    public void setSessionId(long sessionId) {
        this.sessionId = sessionId;
    }

    public void setPlayerId(long playerId) {
        this.playerId = playerId;
    }

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

    private void renderMultipleChoiceAnswers(List<Integer> correctIndices) {
        for (int i = 0; i < multiChoiceAnswers.size(); ++i) {
            if (correctIndices.contains(i)) {
                multiChoiceAnswers.get(i).setStyle("-fx-background-color: green");
            } else {
                multiChoiceAnswers.get(i).setDisable(true);
            }
        }
    }

    private void renderGeneralInformation(Question q) {
        this.questionPrompt.setText(q.prompt);
        // TODO load image
    }

    private void renderAnswerFields(Question q) {
        switch (q.type) {
            case MULTIPLE_CHOICE:
                renderMultipleChoiceQuestion(q);
                break;
            default:
                throw new UnsupportedOperationException("Currently only multiple choice questions can be rendered");
        }
    }

    public void loadQuestion() {
        Question q = this.api.fetchOneQuestion(this.sessionId);
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

    private void renderCorrectAnswer(Evaluation eval) {
        switch (eval.type) {
            case MULTIPLE_CHOICE:
                renderMultipleChoiceAnswers(eval.correctAnswers);
                break;
            default:
                throw new UnsupportedOperationException("Currently only multiple choice answers can be rendered");
        }
    }

    public void gameCleanup() {
        api.removeSession(sessionId);
        this.questionPrompt.setText("[Question]");
        this.answerArea.getChildren().clear();
        this.pointsLabel.setText("Points: 0");
        this.multiChoiceAnswers.clear();
        this.points = 0;
        this.currentQuestion = null;
        disableButton(submitButton, true);
        main.showSplash();
    }

    public void renderPoints() {
        pointsLabel.setText(String.format("Points: %d", this.points));
    }

    public void submitAnswer() {
        /* RadioButton rb = new RadioButton("Answer option #1");
        answerArea.getChildren().add(rb); */
        if (this.timerThread != null && this.timerThread.isAlive()) this.timerThread.interrupt();
        disableButton(submitButton, true);

        Answer ans = new Answer(currentQuestion.type);
        for (int i = 0 ; i < multiChoiceAnswers.size(); ++i) {
            if (multiChoiceAnswers.get(i).isSelected()) {
                ans.addAnswer(i);
            }
        }

        Evaluation eval = api.submitAnswer(sessionId, ans);
        if(doublePointsIsActive()) {
            points += 2 * eval.points;
            switchStatusOfDoublePoints();
        } else {
            points += eval.points;
        }
        renderPoints();
        renderCorrectAnswer(eval);

        // TODO disable button while waiting
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                Platform.runLater(() -> {
                    if (++rounds == GAME_ROUNDS) {
                        // TODO display leaderboard things here
                        gameCleanup();
                    } else {
                        loadQuestion();
                    }
                });
            }
        }, GAME_ROUND_DELAY * 1000);
    }


    /**
     * Disable button so the player can not interact with it
     * @param button - Button to be disabled
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
            case MULTIPLE_CHOICE:
                List<Integer> incorrectAnswers = new ArrayList<>();
                List<Integer> correctAnswers = api.getCorrectAnswers(sessionId);
                for (int i = 0; i < multiChoiceAnswers.size(); ++i) {
                    if (!correctAnswers.contains(i)) {
                        incorrectAnswers.add(i);
                    }
                }
                int randomIndex = new Random().nextInt(incorrectAnswers.size());
                multiChoiceAnswers.get(incorrectAnswers.get(randomIndex)).setDisable(true);
                break;
            default:
                disableButton(removeOneButton, false);
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
    /**
     * Disable the jokers that do not work for single-player
     */
    public void disableSingleplayerJokers() {
        disableButton(decreaseTimeButton, true);
    }
}
