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
     * Starts the default singleplayer game.
     */
    public void showDefault() {
        GameSession newSession = new GameSession(GameSession.SessionType.SINGLEPLAYER);
        newSession = gameSessionUtils.addSession(newSession);
        gameSessionUtils.addPlayer(newSession.id, this.player);

        long playerId = this.player.id;

        if (playerId == 0L) {
            playerId = gameSessionUtils
                    .getPlayers(newSession.id).get(0).id;
        }
        mainCtrl.showDefaultSinglePlayer(newSession.id, playerId);
    }

    /**
     * Starts the survival singleplayer game.
     */
    public void showSurvival() {
        GameSession newSession = new GameSession(GameSession.SessionType.SURVIVAL);
        newSession = gameSessionUtils.addSession(newSession);
        gameSessionUtils.addPlayer(newSession.id, this.player);

        long playerId = this.player.id;

        if (playerId == 0L) {
            playerId = gameSessionUtils
                    .getPlayers(newSession.id).get(0).id;
        }
        gameSessionUtils.setGameRounds(newSession.id, Integer.MAX_VALUE);
        mainCtrl.showSurvival(newSession.id, playerId);
    }

    /**
     * Starts the time attack singleplayer game.
     */
    public void showTimeAttack() {
        GameSession newSession = new GameSession(GameSession.SessionType.TIME_ATTACK);
        newSession = gameSessionUtils.addSession(newSession);
        gameSessionUtils.addPlayer(newSession.id, this.player);

        long playerId = this.player.id;

        if (playerId == 0L) {
            playerId = gameSessionUtils
                    .getPlayers(newSession.id).get(0).id;
        }
        gameSessionUtils.setGameRounds(newSession.id, Integer.MAX_VALUE);
        mainCtrl.showTimeAttack(newSession.id, playerId);
    }

    public void setPlayer(Player player) {
        this.player = player;
    }
}
