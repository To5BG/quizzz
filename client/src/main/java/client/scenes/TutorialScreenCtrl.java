package client.scenes;

import com.google.inject.Inject;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;

import java.util.List;

public class TutorialScreenCtrl extends SceneCtrl{
    private final static double INVISIBLE = 0;
    private final static double DIM = 0.25;
    private final static double VISIBLE = 1;
    private static final int NUMBER_OF_PANES = 5;

    private final MainCtrl mainCtrl;
    @FXML
    protected Button prev;
    @FXML
    protected Button next;
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
    @FXML
    protected Line leftUp;
    @FXML
    protected Line leftDown;
    @FXML
    protected Line rightUp;
    @FXML
    protected Line rightDown;
    protected List<Pane> panes;
    protected List<Circle> dots;
    private int currentNum;


    @Inject
    public TutorialScreenCtrl(MainCtrl mainCtrl) {
        this.mainCtrl = mainCtrl;
    }

    /**
     * Initialise the lists of panes and dots and reset to the first one of each
     */
    public void initialise() {
        panes = List.of(pane0, pane1, pane2, pane3, pane4);
        dots = List.of(dot0, dot1, dot2, dot3, dot4);
        resetCurrentNum();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void shutdown() {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void back() {
        shutdown();
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
     * Switch to the previous pane and circle
     */
    public void switchPrev() {
        if (currentNum == 0) return;

        currentNum--;
        showPane();
    }

    /**
     * Switch to the next pane and circle
     */
    public void switchNext() {
        if (currentNum == NUMBER_OF_PANES - 1) return;

        currentNum++;
        showPane();
    }

    /**
     * Reset the current pane to the first one
     */
    public void resetCurrentNum() {
        this.currentNum = 0;
        showPane();
    }

    /**
     * Make the Pane and respective Circle visible and reset all others.
     */
    private void showPane() {
        for (int i = 0; i < NUMBER_OF_PANES; i++) {
            panes.get(i).setOpacity(INVISIBLE);
            dots.get(i).setOpacity(DIM);
        }

        panes.get(currentNum).setOpacity(VISIBLE);
        dots.get(currentNum).setOpacity(VISIBLE);
        visibleButtons();
    }

    /**
     * Make the prev and next button available (or not) based on what the current pane is
     */
    private void visibleButtons() {
        leftUp.setOpacity(VISIBLE);
        leftDown.setOpacity(VISIBLE);
        prev.setDisable(false);

        rightUp.setOpacity(VISIBLE);
        rightDown.setOpacity(VISIBLE);
        next.setDisable(false);

        if (currentNum == 0) {
            leftUp.setOpacity(DIM);
            leftDown.setOpacity(DIM);
            prev.setDisable(true);
            return;
        }
        if (currentNum == NUMBER_OF_PANES - 1) {
            rightUp.setOpacity(DIM);
            rightDown.setOpacity(DIM);
            next.setDisable(true);
            return;
        }
    }

}
