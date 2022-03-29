package client.scenes;

import client.utils.GameSessionUtils;
import client.utils.LeaderboardUtils;
import client.utils.QuestionUtils;
import commons.Player;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.input.KeyEvent;

import javax.inject.Inject;

public class GamemodeCtrl {

    private final GameSessionUtils gameSessionUtils;
    private final LeaderboardUtils leaderboardUtils;
    private final QuestionUtils questionUtils;
    private final MainCtrl mainCtrl;

    private long sessionId;
    private long playerId;

    @FXML
    private Button defaultButton;

    @FXML
    private Button survivalButton;

    @FXML
    private Button timeAttackButton;

    @FXML
    private Button backButton;

    @Inject
    public GamemodeCtrl(GameSessionUtils gameSessionUtils, LeaderboardUtils leaderboardUtils,
                        QuestionUtils questionUtils, MainCtrl mainCtrl) {
        this.gameSessionUtils = gameSessionUtils;
        this.leaderboardUtils = leaderboardUtils;
        this.questionUtils = questionUtils;
        this.mainCtrl = mainCtrl;

        // Set to defaults
        this.sessionId = 0L;
        this.playerId = 0L;
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
     * Removes player from session. Also called if controller is closed forcibly
     */
    public void shutdown() {
        gameSessionUtils.removePlayer(sessionId, playerId);
        setPlayerId(0);
        setSessionId(0);
    }

    /**
     * Reverts the player to the splash screen and remove him from the current game session.
     */
    public void back() {
        shutdown();
        mainCtrl.showSplash();
    }

    /**
     * Starts the default singleplayer game.
     */
    public void showDefault() {
        mainCtrl.showDefaultSinglePlayer(this.sessionId, this.playerId);
    }

    /**
     * Starts the survival singleplayer game.
     */
    public void showSurvival() {
        //TODO rithik
    }

    /**
     * Starts the time attack singleplayer game.
     */
    public void showTimeAttack() {
        mainCtrl.showDefaultSinglePlayer(this.sessionId, this.playerId);
    }

    /**
     * Setter for sessionId.
     *
     * @param sessionId the id of the sessions
     */
    public void setSessionId(long sessionId) {
        this.sessionId = sessionId;
    }

    /**
     * Setter for playerId.
     *
     * @param playerId the id of the player
     */
    public void setPlayerId(long playerId) {
        this.playerId = playerId;
    }
}
