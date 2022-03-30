package client.scenes;

import client.utils.*;
import commons.Question;
import javafx.application.Platform;
import javafx.collections.FXCollections;

import javax.inject.Inject;
import java.util.Timer;
import java.util.TimerTask;

public class TimeAttackCtrl extends SingleplayerCtrl {

    private final long initialTime = 10L;
    private long timeLeft;
    private long elapsedTime;
    private TimeUtils roundTimer;

    @Inject
    public TimeAttackCtrl(WebSocketsUtils webSocketsUtils, GameSessionUtils gameSessionUtils,
                          LeaderboardUtils leaderboardUtils, QuestionUtils questionUtils, MainCtrl mainCtrl) {
        super(webSocketsUtils, gameSessionUtils, leaderboardUtils, questionUtils, mainCtrl);

        this.timeLeft = 10L;
    }

    /**
     * Displays the question screen attributes.
     */
    @Override
    public void removeMidGameLeaderboard() {
        answerArea.setOpacity(1);
        questionPrompt.setOpacity(1);
        submitButton.setOpacity(1);
    }

    @Override
    public void reset() {
        this.timeLeft = 60L;
        this.elapsedTime = 0;
        super.reset();
    }

    /**
     * Loads the answers of the current question and updates the timer after reading time is over
     */
    @Override
    public void loadAnswer() {
        Question q = this.currentQuestion;
        if (q == null) return;
        renderAnswerFields(q);

        disableButton(submitButton, false);

        roundTimer = new TimeUtils(timeLeft, TIMER_UPDATE_INTERVAL_MS, initialTime);
        roundTimer.setTimeBooster(this::getTimeJokers);
        roundTimer.setOnSucceeded((event) -> Platform.runLater(() -> {
            System.out.println("roundTimer is done");
            this.timeLeft = 0;
            rounds = Integer.MAX_VALUE;
            submitAnswer(true);
        }));

        timeProgress.progressProperty().bind(roundTimer.progressProperty());
        this.timerThread = new Thread(roundTimer);
        this.timerThread.start();
        imageHover();
    }

    /**
     * Submit button click event handler
     */
    @Override
    public void submitAnswerButton() {
        this.elapsedTime = this.roundTimer.getElapsedTime();
        submitAnswer(false);
    }

    /**
     * Gets the user's answer, starts the evaluation and loads a new question or ends the game.
     */
    @Override
    public void startEvaluation() {
        if (this.evaluation == null) return;
        updatePoints();
        renderCorrectAnswer();
        this.evaluation = null;
        this.timeLeft -= this.elapsedTime;

        // TODO disable button while waiting
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                Platform.runLater(() -> {
                    if (currentQuestion == null) return; // happens if shutdown is called before triggering
                    rounds++;
                    if (getTimeLeft() <= 0) {
                        handleGameEnd();
                    } else {
                        handleNextRound();
                    }
                });
            }
        }, GAME_ROUND_DELAY * 1000);
    }

    /**
     * refresh the screen to show the leaderboard.
     */
    public void refresh() {
        var players = leaderboardUtils.getPlayerTimeAttackScore();
        data = FXCollections.observableList(players);
        allPlayers.setItems(data);
    }

    /**
     * Getter for the time left.
     *
     * @return  the time left.
     */
    public long getTimeLeft() {
        return this.timeLeft;
    }

}
