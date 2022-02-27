package client.scenes;

import client.utils.ServerUtils;
import com.google.inject.Inject;
import commons.Answer;
import commons.Evaluation;
import commons.Question;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.layout.Background;
import javafx.scene.layout.StackPane;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class GameCtrl {

    private final int GAME_ROUNDS = 5;

    @FXML
    private StackPane answerArea;

    @FXML
    private Label questionPrompt;

    private ServerUtils api;
    private MainCtrl main;

    private List<RadioButton> multiChoiceAnswers;
    private long sessionId;
    private long playerId;
    private Question currentQuestion;
    private int points = 0;
    private int rounds = 0;

    @Inject
    public GameCtrl(ServerUtils api, MainCtrl main) {
        this.api = api;
        this.main = main;
        this.multiChoiceAnswers = new ArrayList<RadioButton>();
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
        this.multiChoiceAnswers.clear();
        this.points = 0;
        this.currentQuestion = null;
        main.showSplash();
    }

    public void submitAnswer() {
        /* RadioButton rb = new RadioButton("Answer option #1");
        answerArea.getChildren().add(rb); */
        Answer ans = new Answer(currentQuestion.type);
        for (int i = 0 ; i < multiChoiceAnswers.size(); ++i) {
            if (multiChoiceAnswers.get(i).isSelected()) {
                ans.addAnswer(i);
            }
        }

        Evaluation eval = api.submitAnswer(sessionId, ans);
        points += eval.points;
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
        }, 5000);
    }
}
