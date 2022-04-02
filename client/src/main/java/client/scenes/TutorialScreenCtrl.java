package client.scenes;

import com.google.inject.Inject;
import javafx.fxml.FXML;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Circle;

import java.util.List;

public class TutorialScreenCtrl{
    private final static double CIRCLE_DEFAULT = 0.25;
    private final static double CIRCLE_SELECTED = 0.80;
    private static final int NUMBER_OF_PANES = 5;

    private final MainCtrl mainCtrl;

    private int currentNum;

    @FXML
    protected Pane pane0;
    @FXML
    protected Circle dot0;
    @FXML
    protected Pane pane1;
    @FXML
    protected Circle dot1;
    @FXML
    protected Pane pane2;
    @FXML
    protected Circle dot2;
    @FXML
    protected Pane pane3;
    @FXML
    protected Circle dot3;
    @FXML
    protected Pane pane4;
    @FXML
    protected Circle dot4;
    
    protected List<Pane> panes;
    protected List<Circle> circles;


    @Inject
    public TutorialScreenCtrl(MainCtrl mainCtrl) {
        this.mainCtrl = mainCtrl;
    }

    /**
     * Initialise the lists and reset to the first one
     */
    public void initialise() {
        panes = List.of(pane0, pane1, pane2, pane3, pane4);
        circles = List.of(dot0, dot1, dot2, dot3, dot4);
        resetCurrentNum();
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

    /**
     * Switch to the previous circle
     */
    public void switchPrev() {
        if(currentNum == 0) return;

        currentNum --;
        showPane(currentNum);
    }

    /**
     * Switch to the next pane
     */
    public void switchNext() {
        if(currentNum == 4) return;

        currentNum ++;
        showPane(currentNum);
    }

    /**
     * Reset the current pane to the first one
     */
    public void resetCurrentNum() {
        this.currentNum = 0;
        showPane(0);
    }

    /**
     * Make the Pane and respective Circle visible and reset all others.
     * @param num - index of the pane that will be shown
     */
    private void showPane(int num) {
        for(int i = 0; i < NUMBER_OF_PANES; i++) {
            panes.get(i).setOpacity(0);
            circles.get(i).setOpacity(CIRCLE_DEFAULT);
        }

        panes.get(num).setOpacity(1);
        circles.get(num).setOpacity(CIRCLE_SELECTED);

    }
}
