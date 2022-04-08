package client.scenes;

import client.utils.*;
import com.google.inject.Inject;
import commons.Emoji;
import commons.GameSession;
import commons.Player;
import jakarta.ws.rs.BadRequestException;
import javafx.application.Platform;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.util.Callback;
import org.springframework.messaging.simp.stomp.StompSession;

import java.net.URL;
import java.nio.file.Path;
import java.util.*;

import static client.scenes.GameCtrl.*;

public class EndGameScreenCtrl extends SceneCtrl implements Initializable {

    private final static long END_GAME_TIME = 60L;

    private final GameSessionUtils gameSessionUtils;
    private final WebSocketsUtils webSocketsUtils;
    private final GameAnimation gameAnimation;
    private final MainCtrl mainCtrl;

    @FXML
    protected Button playAgain;
    @FXML
    protected Button backButton;
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
    @FXML
    private ImageView emojiFunny;
    @FXML
    private ImageView emojiSad;
    @FXML
    private ImageView emojiAngry;
    @FXML
    private Pane emojiArea;

    private int previousPlayerCount;
    protected Thread timerThread;
    private long sessionId;
    private long playerId;
    private int waitingSkip = 0;
    private boolean playingAgain;
    private Timer endGameTimer;
    private TimeUtils roundTimer;
    private StompSession.Subscription channelEnd;
    private final List<Image> emojiImages;


    @Inject
    public EndGameScreenCtrl(GameSessionUtils gameSessionUtils, GameAnimation gameAnimation,
                             WebSocketsUtils webSocketsUtils, MainCtrl mainCtrl) {
        this.gameSessionUtils = gameSessionUtils;
        this.gameAnimation = gameAnimation;
        this.webSocketsUtils = webSocketsUtils;
        this.mainCtrl = mainCtrl;
        emojiImages = new ArrayList<Image>();
        String[] emojiFileNames = {"funny", "sad", "angry"};
        ClassLoader cl = getClass().getClassLoader();
        for (String fileName : emojiFileNames) {
            URL location = cl.getResource(
                    Path.of("", "client", "scenes", "emojis", fileName + ".png").toString());

            emojiImages.add(new Image(location.toString()));
        }
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

        emojiFunny.setImage(emojiImages.get(0));
        emojiSad.setImage(emojiImages.get(1));
        emojiAngry.setImage(emojiImages.get(2));
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
                        } else if (gameSessionUtils.getSession(sessionId).sessionStatus
                                == GameSession.SessionStatus.TRANSFERRING) {
                            cancel();
                        }
                    } catch (Exception e) {
                        cancel();
                    }
                });
            }

        }, 0, 100);
    }

    /**
     * {@inheritDoc}
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
        channelEnd.unsubscribe();
    }

    /**
     * {@inheritDoc}
     */
    public void back() {
        shutdown();
        reset();
        mainCtrl.showSplash();
    }

    /**
     * Resets the end of game screen.
     */
    public void reset() {
        playAgain.setText("Play again");
        status.setText("End of game! Play again or go back to main.");
        setPlayingAgain(false);
        waitingSkip = 0;
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
        //gameSessionUtils.toggleReady(sessionId, false);
        var players = gameSessionUtils.getPlayers(sessionId);
        var data = FXCollections.observableList(players);
        leaderboard.setItems(data);

        roundTimer = new TimeUtils(END_GAME_TIME, TIMER_UPDATE_INTERVAL_MS);
        registerForEmojiUpdates();
        roundTimer.setTimeBooster(() -> (double) waitingSkip);
        roundTimer.setOnSucceeded((event) -> {
            gameSessionUtils.updateStatus(gameSessionUtils.getSession(sessionId),
                    GameSession.SessionStatus.TRANSFERRING);
            Platform.runLater(() -> {
                if (isPlayingAgain()) {
                    startGame();
                } else {
                    back();
                }
            });
        });

        progressBar.progressProperty().bind(roundTimer.progressProperty());
        this.timerThread = new Thread(roundTimer);
        this.timerThread.start();
        scanForEndGameAddition();
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
                        back();
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

    /**
     * Register the client to receive emoji reactions from other players
     */
    public void registerForEmojiUpdates() {
        channelEnd = this.webSocketsUtils.registerForEmojiUpdates(emoji -> {
            Platform.runLater(() -> gameAnimation.startEmojiAnimation(
                    gameAnimation.emojiToImage(emojiImages, emoji, 60), emoji.username, emojiArea));
        }, this.sessionId);
    }

    /**
     * Generic event handler for clicking on an emoji
     *
     * @param ev The event information
     */
    public void emojiEventHandler(Event ev) {
        Node source = (Node) ev.getSource();
        String nodeId = source.getId();
        Emoji.EmojiType type;
        switch (nodeId) {
            case "emojiFunny":
                type = Emoji.EmojiType.FUNNY;
                break;
            case "emojiSad":
                type = Emoji.EmojiType.SAD;
                break;
            case "emojiAngry":
                type = Emoji.EmojiType.ANGRY;
                break;
            default:
                return;
        }
        webSocketsUtils.sendEmoji(sessionId, playerId, type);
    }

    /**
     * Scans for players joining in the end game screen
     */
    public void scanForEndGameAddition() {
        previousPlayerCount = -1;
        endGameTimer = new Timer();
        endGameTimer.scheduleAtFixedRate(new TimerTask() {

            @Override
            public void run() {
                int playerCount = gameSessionUtils.getSession(sessionId).players.size();
                if (previousPlayerCount < playerCount) {
                    roundTimer.resetTimer();
                    Platform.runLater(() -> renderLeaderboard());
                }
                previousPlayerCount = playerCount;
            }
        }, 0, 500);
    }

    /**
     * Updates the items in the leaderboard and makes sure the leaderboard remains visible
     */
    public void renderLeaderboard() {
        var players = gameSessionUtils.getPlayers(sessionId);
        var data = FXCollections.observableList(players);
        leaderboard.setItems(data);
    }

}
