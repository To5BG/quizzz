package client.scenes;

import client.utils.ServerUtils;
import com.google.inject.Inject;
import commons.Question;
import javafx.fxml.FXML;
import javafx.scene.control.RadioButton;
import javafx.scene.layout.StackPane;

import java.util.ArrayList;
import java.util.List;

public class GameCtrl {

    @FXML
    private StackPane answerArea;

    private ServerUtils api;
    private MainCtrl main;

    private List<RadioButton> multiChoiceAnswers;
    private long sessionId;
    private long playerId;

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

    private void renderMultipleChoice(Question q) {
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

    public void renderAnswerFields(Question q) {
        switch (q.type) {
            case MULTIPLE_CHOICE:
                renderMultipleChoice(q);
                break;
            default:
                throw new UnsupportedOperationException("Currently only multiple choice questions can be rendered");
        }
    }

    public void submitAnswer() {
        /* RadioButton rb = new RadioButton("Answer option #1");
        answerArea.getChildren().add(rb); */
        Question q = this.api.fetchOneQuestion(this.sessionId);
        renderAnswerFields(q);
    }
}
