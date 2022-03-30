package client.scenes;

import client.utils.*;
import com.google.inject.Inject;
import commons.GameSession;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TableView;

import java.net.URL;
import java.util.ResourceBundle;

public class EndGameScreenCtrl extends MultiplayerCtrl implements Initializable {

    @FXML
    protected Button playAgain;
    @FXML
    protected Button leave;
    @FXML
    protected ProgressBar progressBar;
    @FXML
    protected Label status;
    @FXML
    protected TableView leaderboard;

    private final static long END_GAME_TIME = 60L;
    private int waitingSkip = 0;
    protected Thread timerThread;
    /**
     * Called to initialize a controller after its root element has been
     * completely processed.
     *
     * @param location  The location used to resolve relative paths for the root object, or
     *                  {@code null} if the location is not known.
     * @param resources The resources used to localize the root object, or {@code null} if
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
    }

    @Inject
    public EndGameScreenCtrl (GameSessionUtils gameSessionUtils, WebSocketsUtils webSocketsUtils,
                              LeaderboardUtils leaderboardUtils, QuestionUtils questionUtils, MainCtrl mainCtrl) {
        super(webSocketsUtils, gameSessionUtils, leaderboardUtils, questionUtils, mainCtrl);
    }

    /**
     * the method to show the end game screen
     * @param sessionId the id of the current game session
     */
    public void showEndGameScreen(Long sessionId) {
        System.out.println("test successful");

        renderLeaderboard();
        TimeUtils roundTimer = new TimeUtils(END_GAME_TIME, TIMER_UPDATE_INTERVAL_MS);
        roundTimer.setTimeBooster(() -> (double) waitingSkip);
        roundTimer.setOnSucceeded((event) -> {
            gameSessionUtils.updateStatus(gameSessionUtils.getSession(sessionId),
                    GameSession.SessionStatus.TRANSFERRING);
            Platform.runLater(() -> {
                if (isPlayingAgain()) {
                    startGame();
                } else {
                    leaveGame();
                }
            });
        });

        progressBar.progressProperty().bind(roundTimer.progressProperty());
        this.timerThread = new Thread(roundTimer);
        this.timerThread.start();

        gameSessionUtils.toggleReady(sessionId, false);
        refresh();
    }

    public void playAgain() {
        super.playAgain();
    }

    public void back() {
        super.back();
    }
}
