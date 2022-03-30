package client.scenes;

import client.utils.*;
import commons.Answer;
import commons.Question;
import jakarta.ws.rs.BadRequestException;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Label;

import javax.inject.Inject;
import java.util.Timer;
import java.util.TimerTask;

public class SurvivalCtrl extends SingleplayerCtrl {

    private int gamelives;

    @FXML
    private Label livesLabel;

    @Inject
    public SurvivalCtrl(WebSocketsUtils webSocketsUtils, GameSessionUtils gameSessionUtils,
                        LeaderboardUtils leaderboardUtils, QuestionUtils questionUtils, MainCtrl mainCtrl) {
        super(webSocketsUtils, gameSessionUtils, leaderboardUtils, questionUtils, mainCtrl);
        this.gamelives = 3;
    }

    /**
     * Resets all fields and the screen for a new game.
     */
    public void reset() {
        this.gamelives = 3;
        super.reset();
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

    /**
     * Displays the number of lives the player still has
     */
    public void renderLivesCount() {
        livesLabel.setText(String.format("Lives: %d", gamelives));
    }

    /**
     * Loads a question and starts reading time.
     */
    public void loadQuestion() {
        disableButton(submitButton, true);
        this.answerArea.getChildren().clear();

        try {
            Question q = this.questionUtils.fetchOneQuestion(this.sessionId);
            this.currentQuestion = q;
            renderGeneralInformation(q);
            renderQuestionCount();
            renderLivesCount();
            countdown();
        } catch (BadRequestException ignore) { /* happens when session is removed before question is loaded */ }
    }

    /**
     * Submit button click event handler
     */
    @Override
    public void submitAnswerButton() {
        submitAnswer(false);
    }

    /**
     * Submit an answer to the server
     */
    @Override
    public void submitAnswer(boolean initiatedByTimer) {
        Answer ans = new Answer(currentQuestion.type);

        for (int i = 0; i < multiChoiceAnswers.size(); ++i) {
            if (multiChoiceAnswers.get(i).isSelected()) {
                ans.addAnswer(i);
            }
        }

        if (this.timerThread != null && this.timerThread.isAlive()) this.timerThread.interrupt();
        disableButton(submitButton, true);

        this.evaluation = questionUtils.submitAnswer(sessionId, playerId, ans);

        gameSessionUtils.toggleReady(sessionId, true);
        startEvaluation();
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

        // TODO disable button while waiting
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                Platform.runLater(() -> {
                    if (currentQuestion == null) return; // happens if shutdown is called before triggering
                    rounds++;
                    if (gamelives == 0) {
                        handleGameEnd();
                    } else {
                        handleNextRound();
                    }
                });
            }
        }, GAME_ROUND_DELAY * 1000);
    }

    /**
     * Updates the point counter in client side, and then updates database entry
     */
    public void updatePoints() {
        if (evaluation.points == 0) {
            gamelives--;
        }
        points += evaluation.points;
        renderStreak();
    }

    /**
     * Called when player points are rendered or updated
     */
    public void renderStreak() {
        pointsLabel.setText(String.format("Current Streak: %d", this.points));
    }

    /**
     * refresh the screen to show the leaderboards
     */
    @Override
    public void refresh() {
        var players = leaderboardUtils.getPlayerSurvivalScore();
        data = FXCollections.observableList(players);
        allPlayers.setItems(data);
    }
}
