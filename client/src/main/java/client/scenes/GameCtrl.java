package client.scenes;

import javafx.fxml.FXML;
import javafx.scene.control.RadioButton;
import javafx.scene.layout.StackPane;

public class GameCtrl {

    @FXML
    private StackPane answerArea;

    public void submitAnswer() {
        RadioButton rb = new RadioButton("Answer option #1");
        answerArea.getChildren().add(rb);
    }
}
