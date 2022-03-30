package client.scenes;

import client.utils.*;
import com.google.inject.Inject;
import commons.GameSession;
import commons.Player;
import jakarta.ws.rs.BadRequestException;
import javafx.application.Platform;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.util.Callback;

import java.net.URL;
import java.util.ResourceBundle;
import java.util.Timer;
import java.util.TimerTask;

import static client.scenes.GameCtrl.TIMER_UPDATE_INTERVAL_MS;

public class EndGameScreenCtrl implements Initializable {

    private final static long END_GAME_TIME = 60L;
    private final GameSessionUtils gameSessionUtils;
    private final LeaderboardUtils leaderboardUtils;
    private final QuestionUtils questionUtils;
    private final WebSocketsUtils webSocketsUtils;
    private final MainCtrl mainCtrl;
    @FXML
    protected Button playAgain;
    @FXML
    protected Button leave;
    @FXML
    protected ProgressBar progressBar;
    @FXML
    protected Label count;
    @FXML
    protected Label status;
    @FXML
    protected TableView<Player> leaderboard;
    @FXML
    protected TableColumn<Player, Integer> colRank;
    @FXML
    protected TableColumn<Player, String> colUserName;
    @FXML
    protected TableColumn<Player, Integer> colPoints;
    protected Thread timerThread;
    private long sessionId;
    private long playerId;
    private int waitingSkip = 0;
    private boolean playingAgain;


    @Inject
    public EndGameScreenCtrl(GameSessionUtils gameSessionUtils, WebSocketsUtils webSocketsUtils,
                             LeaderboardUtils leaderboardUtils, QuestionUtils questionUtils, MainCtrl mainCtrl) {
        this.gameSessionUtils = gameSessionUtils;
        this.leaderboardUtils = leaderboardUtils;
        this.webSocketsUtils = webSocketsUtils;
        this.questionUtils = questionUtils;
        this.mainCtrl = mainCtrl;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        colUserName.setCellValueFactory(p -> new SimpleStringProperty(p.getValue().username));
        colPoints.setCellValueFactory(p -> new SimpleIntegerProperty(p.getValue().currentPoints).asObject());

        colRank.setCellFactory(new Callback<>() {
            @Override
            public TableCell<Player, Integer> call(TableColumn<Player, Integer> param) {
                return new TableCell<>() {
                    @Override
                    protected void updateItem(Integer item, boolean empty) {
                        super.updateItem(item, empty);
                        if (!empty) setText(this.getTableRow().getIndex() + 1 + "");
                        else setText("");
                    }
                };
            }
        });

        playAgain.setOpacity(1);
        count.setText("Waiting for game to start...");
        count.setOpacity(1);
        leave.setOpacity(1);
    }

    /**
     * Refreshes the end of game screen to check how many players want to play again.
     */
    public void refresh() {
        Timer timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                Platform.runLater(() -> {
                    try {
                        if (gameSessionUtils.getSession(sessionId).sessionStatus
                                == GameSession.SessionStatus.PLAY_AGAIN) {
                            if (gameSessionUtils.getSession(sessionId).players.size() ==
                                    gameSessionUtils.getSession(sessionId).playersReady.get()) {
                                //Speed the timer up
                                waitingSkip = 4;
                            } else {
                                //Slow the timer down
                                waitingSkip = 0;
                            }
                            count.setText(gameSessionUtils.getSession(sessionId).playersReady.get() + " / " +
                                    gameSessionUtils.getSession(sessionId).players.size()
                                    + " players want to play again");
                        }
                        if (gameSessionUtils.getSession(sessionId).sessionStatus
                                == GameSession.SessionStatus.TRANSFERRING) {
                            cancel();
                        }
                    } catch (Exception e) {
                        cancel();
                    }
                });
            }

            @Override
            public boolean cancel() {
                return super.cancel();
            }
        }, 0, 100);
    }

    /**
     * Removes a player from the session and sets unready of the player was ready
     */
    public void shutdown() {
        if (this.timerThread != null && this.timerThread.isAlive()) this.timerThread.interrupt();
        if (playAgain.getText().equals("Don't play again")) {
            playAgain();
        }
        if (sessionId != 0) {
            try {
                gameSessionUtils.removePlayer(sessionId, playerId);
            } catch (BadRequestException ignore) { /* session might be removed at this point */ }
            setPlayerId(0);
        }
        setSessionId(0);
    }

    /**
     * Resets the end of game screen.
     */
    public void reset() {
        playAgain.setText("Play again");
        playAgain.setOpacity(0);
        leave.setOpacity(0);
        leave.setDisable(true);
        count.setText("[Count]");
        count.setText("Waiting for game to start...");
        setPlayingAgain(false);
        waitingSkip = 0;
        setPlayingAgain(false);
    }

    /**
     * Reverts the player to the splash screen.
     */
    public void leaveGame() {
        shutdown();
        reset();
        mainCtrl.showSplash();
    }

    /**
     * Toggles between want to play again and don't want to play again, modifying playAgain button and stores whether
     * the player wants to play again.
     */
    public void playAgain() {
        switch (playAgain.getText()) {
            case "Play again" -> {
                playAgain.setText("Don't play again");
                status.setText("Waiting for game to start...");
                gameSessionUtils.toggleReady(sessionId, true);
                setPlayingAgain(true);
            }
            case "Don't play again" -> {
                playAgain.setText("Play again");
                status.setText("End of game! Play again or go back to main.");
                gameSessionUtils.toggleReady(sessionId, false);
                setPlayingAgain(false);
            }
        }
    }

    /**
     * Starts the timer and sets all players unready.
     */
    public void showEndScreen() {
        gameSessionUtils.toggleReady(sessionId, false);
        var players = gameSessionUtils.getPlayers(sessionId);
        var data = FXCollections.observableList(players);
        leaderboard.setItems(data);

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
        refresh();
    }

    /**
     * Checks whether there are enough players in the session after the clients had time to remove the players that
     * quit.
     */
    public void startGame() {
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                Platform.runLater(() -> {
                    if (gameSessionUtils.getPlayers(sessionId).size() >= 2 && isPlayingAgain()) {
                        GameSession session = gameSessionUtils.toggleReady(sessionId, false);
                        if (session.playersReady.get() == 0) {
                            gameSessionUtils.updateStatus(session, GameSession.SessionStatus.ONGOING);
                        }
                        reset();
                        mainCtrl.showMultiplayer(sessionId, playerId);
                    } else {
                        leaveGame();
                        Alert alert = new Alert(Alert.AlertType.INFORMATION);
                        alert.setTitle("Unable to start new game!");
                        alert.setHeaderText("There are too few people to play again:");
                        alert.setContentText("Please join a fresh game to play with more people!");
                        alert.showAndWait();
                    }
                });
            }
        }, 1000);
    }

    /**
     * Getter for playingAgain field.
     *
     * @return whether the player wants to play again.
     */
    public boolean isPlayingAgain() {
        return playingAgain;
    }

    /**
     * Setter for playingAgain field
     *
     * @param playingAgain parameter that shows if a player wants to play again.
     */
    public void setPlayingAgain(boolean playingAgain) {
        this.playingAgain = playingAgain;
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
