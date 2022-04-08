package client.scenes;

import client.utils.GameSessionUtils;
import client.utils.LeaderboardUtils;
import client.utils.QuestionUtils;
import client.utils.WebSocketsUtils;
import commons.Answer;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Label;

import javax.inject.Inject;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.Timer;
import java.util.TimerTask;

public class SurvivalCtrl extends SingleplayerCtrl {

    private long gameLives;

    @FXML
    private Label livesLabel;

    @Inject
    public SurvivalCtrl(WebSocketsUtils webSocketsUtils, GameSessionUtils gameSessionUtils,
                        LeaderboardUtils leaderboardUtils, QuestionUtils questionUtils,
                        GameAnimation gameAnimation, SoundManager soundManager, MainCtrl mainCtrl) {
        super(webSocketsUtils, gameSessionUtils, leaderboardUtils,
                questionUtils, gameAnimation, soundManager, mainCtrl);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        colName.setCellValueFactory(p -> new SimpleStringProperty(p.getValue().username));
        colPoint.setCellValueFactory(q -> new SimpleStringProperty(String.valueOf(q.getValue().bestSurvivalScore)));
    }

    /**
     * Resets all fields and the screen for a new game.
     */
    public void reset() {
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
        livesLabel.setText(String.format("Lives: %d", gameLives));
    }

    /**
     * Loads a question and starts reading time.
     */
    public void loadQuestion() {
        renderLivesCount();
        super.loadQuestion();
    }

    /**
     * Sets live count for current game session
     * @param lives lives count to set the game session with
     */
    public void setLives(double lives) {
        this.gameLives = Math.round(lives);
        if (gameLives != 3) gameSessionUtils.disableLeaderboard(sessionId);
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

        if (evaluation.points == 0) {
            gameLives--;
        }
        if (gameLives == 0) {
            gameSessionUtils.setQuestionCounter(sessionId, Integer.MAX_VALUE);
        }
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
                    if (gameLives == 0) {
                        handleGamePodium();
                    } else {
                        handleNextRound();
                    }
                });
            }
        }, GAME_ROUND_DELAY * 1000);
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
