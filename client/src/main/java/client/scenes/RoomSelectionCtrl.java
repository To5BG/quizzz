package client.scenes;

import client.utils.GameSessionUtils;
import com.google.inject.Inject;
import javafx.scene.input.KeyEvent;

public class RoomSelectionCtrl {

    private final GameSessionUtils gameSessionUtils;
    private final MainCtrl mainCtrl;


    @Inject
    public RoomSelectionCtrl(GameSessionUtils gameSessionUtils, MainCtrl mainCtrl) {
        this.gameSessionUtils = gameSessionUtils;
        this.mainCtrl = mainCtrl;
    }

    /**
     * Reverts the player to the splash screen.
     */
    public void back() {
        mainCtrl.showSplash();
    }

    /**
     * Switch method that maps keyboard key presses to functions.
     * @param e KeyEvent to be switched
     */
    public void keyPressed(KeyEvent e) {
        switch (e.getCode()) {
            case ESCAPE -> back();
        }
    }

    /**
     * Refresh the list of available waiting rooms
     * @return True iff the refresh should continue
     */
    public boolean refresh() {
        return true;
    }
}
