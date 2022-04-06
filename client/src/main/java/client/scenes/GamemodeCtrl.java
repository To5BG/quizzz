package client.scenes;

import client.utils.GameSessionUtils;
import commons.GameSession;
import commons.Player;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.input.KeyEvent;

import javax.inject.Inject;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class GamemodeCtrl implements Initializable {

    private final GameSessionUtils gameSessionUtils;
    private final MainCtrl mainCtrl;
    private final GameAnimation gameAnimation;
    @FXML
    protected Button defaultButton;
    @FXML
    protected Button survivalButton;
    @FXML
    protected Button timeAttackButton;
    private long playerId;

    @Inject
    public GamemodeCtrl(GameSessionUtils gameSessionUtils, MainCtrl mainCtrl) {
        this.gameSessionUtils = gameSessionUtils;
        this.mainCtrl = mainCtrl;
        gameAnimation = new GameAnimation();
    }

    /**
     * Enable the animation for the gameButtons
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        gameAnimation.startBatteryAnimation(List.of(defaultButton, survivalButton, timeAttackButton));
    }

    /**
     * Setter for playerId
     *
     * @param playerId - Id for the player
     */
    public void setPlayerId(long playerId) {
        this.playerId = playerId;
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
        gameSessionUtils.removePlayer(MainCtrl.SELECTION_ID, playerId);
    }

    /**
     * Reverts the player to the splash screen and remove him from the current game session.
     */
    public void back() {
        shutdown();
        mainCtrl.showSplash();
    }

    /**
     * Creates a session for the mode that is called and adds the player to it.
     *
     * @param type The type of game the player wants to play.
     * @return The sessionId of the new session.
     */
    public long createId(GameSession.SessionType type) {
        Player player = gameSessionUtils.removePlayer(MainCtrl.SELECTION_ID, playerId);
        GameSession newSession = new GameSession(type);
        newSession = gameSessionUtils.addSession(newSession);
        gameSessionUtils.addPlayer(newSession.id, player);

        return newSession.id;
    }

    /**
     * Starts the default singleplayer game.
     */
    public void showDefault() {
        long sessionId = createId(GameSession.SessionType.SINGLEPLAYER);
        mainCtrl.showDefaultSinglePlayer(sessionId, playerId);
    }

    /**
     * Starts the survival singleplayer game.
     */
    public void showSurvival() {
        long sessionId = createId(GameSession.SessionType.SURVIVAL);
        gameSessionUtils.setGameRounds(sessionId, Integer.MAX_VALUE);
        mainCtrl.showSurvival(sessionId, playerId);
    }

    /**
     * Starts the time attack singleplayer game.
     */
    public void showTimeAttack() {
        long sessionId = createId(GameSession.SessionType.TIME_ATTACK);
        gameSessionUtils.setGameRounds(sessionId, Integer.MAX_VALUE);
        mainCtrl.showTimeAttack(sessionId, playerId);
    }

}
