package client.scenes;

import client.utils.ServerUtils;

import javax.inject.Inject;

public class SingleplayerCtrl extends GameCtrl{

    @Inject
    public SingleplayerCtrl(ServerUtils server, MainCtrl mainCtrl) {
        super(server, mainCtrl);
    }

    /**
     * Submit an answer to the server and start evaluation
     */
    @Override
    public void submitAnswer() {
        super.submitAnswer();
        startEvaluation();
    }

    /**
     * Reverts the player to the splash screen and remove him from the current game session.
     */
    @Override
    public void back() {
        super.back();
        this.questionPrompt.setText("[Question]");
        this.answerArea.getChildren().clear();
        this.pointsLabel.setText("Points: 0");
        this.multiChoiceAnswers.clear();
        this.points = 0;
        this.rounds = 0;
        this.currentQuestion = null;
        disableButton(submitButton, true);
    }

    /**
     * Disable the jokers that do not work for single-player
     */
    public void disableSingleplayerJokers() {
        disableButton(decreaseTimeButton, true);
    }
}
