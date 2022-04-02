package client.scenes;

import com.google.inject.Inject;
import javafx.scene.input.KeyEvent;

public class TutorialScreenCtrl{

    private final MainCtrl mainCtrl;

    @Inject
    public TutorialScreenCtrl(MainCtrl mainCtrl) {
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
     *
     * @param e KeyEvent to be switched
     */
    public void keyPressed(KeyEvent e) {
        switch (e.getCode()) {
            case ESCAPE -> back();
        }
    }
}
