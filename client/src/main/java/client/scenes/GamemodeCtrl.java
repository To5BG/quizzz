package client.scenes;

import client.utils.GameSessionUtils;
import commons.GameSession;
import commons.Player;
import javafx.scene.input.KeyEvent;

import javax.inject.Inject;

public class GamemodeCtrl {

    private final GameSessionUtils gameSessionUtils;
    private final MainCtrl mainCtrl;

    private Player player;

    @Inject
    public GamemodeCtrl(GameSessionUtils gameSessionUtils, MainCtrl mainCtrl) {
        this.gameSessionUtils = gameSessionUtils;
        this.mainCtrl = mainCtrl;
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
        this.player = null;
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
        GameSession newSession = new GameSession(type);
        newSession = gameSessionUtils.addSession(newSession);
        gameSessionUtils.addPlayer(newSession.id, this.player);

        if (this.player.id == 0L) this.player.id = gameSessionUtils.getPlayers(newSession.id).get(0).id;
        return newSession.id;
    }

    /**
     * Starts the default singleplayer game.
     */
    public void showDefault() {
        mainCtrl.showDefaultSinglePlayer(createId(GameSession.SessionType.SINGLEPLAYER), this.player.id);
    }

    /**
     * Starts the survival singleplayer game.
     */
    public void showSurvival() {
        mainCtrl.showDefaultSinglePlayer(createId(GameSession.SessionType.SURVIVAL), this.player.id);
    }

    /**
     * Starts the time attack singleplayer game.
     */
    public void showTimeAttack() {
        mainCtrl.showDefaultSinglePlayer(createId(GameSession.SessionType.TIME_ATTACK), this.player.id);
    }

    public void setPlayer(Player player) {
        this.player = player;
    }
}
