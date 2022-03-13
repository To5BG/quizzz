package client.scenes;

import client.utils.ServerUtils;

import javax.inject.Inject;
import java.net.URL;
import java.util.ResourceBundle;

public class SingleplayerCtrl extends GameCtrl {

    @Inject
    public SingleplayerCtrl(ServerUtils server, MainCtrl mainCtrl) {
        super(server, mainCtrl);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
    }

    /**
     * Submit an answer to the server and start evaluation
     */
    @Override
    public void submitAnswer(boolean initiatedByTimer) {
        super.submitAnswer(initiatedByTimer);
        startSingleEvaluation();
    }

    /**
     * Enables the doublePoints joker if still available and calls the overridden method
     */
    @Override
    public void loadAnswer() {
        if(doublePointsJoker) {
            disableButton(doublePointsButton, false);
        }
        super.loadAnswer();
    }
    /**
     * Reverts the player to the splash screen and remove him from the current game session.
     */
    @Override
    public void back() {
        super.back();
    }

    /**
     * Disable the jokers that do not work for single-player
     */
    public void disableSingleplayerJokers() {
        decreaseTimeJoker = false;
        disableButton(decreaseTimeButton, true);
    }
}
