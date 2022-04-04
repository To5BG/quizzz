package client.scenes;

import client.utils.*;
import com.google.inject.Inject;
import commons.Emoji;
import commons.GameSession;
import commons.Player;
import jakarta.ws.rs.BadRequestException;
import javafx.application.Platform;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.util.Callback;
import org.springframework.messaging.simp.stomp.StompSession;

import java.net.URL;
import java.nio.file.Path;
import java.util.*;

import static client.scenes.GameCtrl.TIMER_UPDATE_INTERVAL_MS;

public class EndGameScreenCtrl implements Initializable {

    private final static long END_GAME_TIME = 60L;
    private final GameSessionUtils gameSessionUtils;
    private final LeaderboardUtils leaderboardUtils;
    private final QuestionUtils questionUtils;
    private final WebSocketsUtils webSocketsUtils;
    private final MainCtrl mainCtrl;
    private final ObservableList<Emoji> sessionEmojis;
    private final List<Image> emojiImages;
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
    @FXML
    private TableView<Emoji> emojiList;
    @FXML
    private TableColumn<Emoji, String> emojiUsername;
    @FXML
    private TableColumn<Emoji, ImageView> emojiImage;
    @FXML
    private ImageView emojiFunny;
    @FXML
    private ImageView emojiSad;
    @FXML
    private ImageView emojiAngry;
    protected Thread timerThread;
    private long sessionId;
    private long playerId;
    private int waitingSkip = 0;
    private boolean playingAgain;
    private StompSession.Subscription channelEnd;


    @Inject
    public EndGameScreenCtrl(GameSessionUtils gameSessionUtils, WebSocketsUtils webSocketsUtils,
                             LeaderboardUtils leaderboardUtils, QuestionUtils questionUtils, MainCtrl mainCtrl) {
        this.gameSessionUtils = gameSessionUtils;
        this.leaderboardUtils = leaderboardUtils;
        this.webSocketsUtils = webSocketsUtils;
        this.questionUtils = questionUtils;
        this.mainCtrl = mainCtrl;
        sessionEmojis = FXCollections.observableArrayList();
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

        playAgain.setOpacity(1);
        count.setText("Waiting for game to start...");
        count.setOpacity(1);
        leave.setOpacity(1);

        emojiUsername.setCellValueFactory(e -> new SimpleStringProperty(e.getValue().username));
        emojiImage.setCellValueFactory(e -> {
            Image picture;
            switch (e.getValue().emoji) {
                case FUNNY -> picture = emojiImages.get(0);
                case SAD -> picture = emojiImages.get(1);
                default -> picture = emojiImages.get(2);
            }

            ImageView iv = new ImageView(picture);
            iv.setFitHeight(30);
            iv.setFitWidth(30);
            return new SimpleObjectProperty<ImageView>(iv);
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
        channelEnd.unsubscribe();
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
        registerForEmojiUpdates();
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

    /**
     * Register the client to receive emoji reactions from other players
     */
    public void registerForEmojiUpdates() {
        sessionEmojis.clear();
        emojiList.setItems(sessionEmojis);

        channelEnd = this.webSocketsUtils.registerForEmojiUpdates(emoji -> {
            sessionEmojis.add(emoji);
            Platform.runLater(() -> emojiList.scrollTo(sessionEmojis.size() - 1));
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
}
