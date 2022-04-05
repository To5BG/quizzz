/*
 * Copyright 2021 Delft University of Technology
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package client.scenes;

import client.utils.*;
import com.google.inject.Inject;
import commons.*;
import jakarta.ws.rs.BadRequestException;
import javafx.application.Platform;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.util.Callback;
import org.springframework.messaging.simp.stomp.StompSession;

import java.net.URL;
import java.nio.file.Path;
import java.util.*;

public class MultiplayerCtrl extends GameCtrl {

    private final ObservableList<Emoji> sessionEmojis;
    private final List<Image> emojiImages;
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
    @FXML
    private Button backButton;
    @FXML
    private Label removedPlayers;
    @FXML
    private Label jokerUsage;

    private int lastDisconnectIndex;
    private Timer disconnectTimer;
    private int lastJokerIndex;
    private Timer jokerTimer;
    private StompSession.Subscription channel;

    private List<Joker> usedJokers;

    @Inject
    public MultiplayerCtrl(WebSocketsUtils webSocketsUtils, GameSessionUtils gameSessionUtils,
                           LeaderboardUtils leaderboardUtils, QuestionUtils questionUtils, MainCtrl mainCtrl) {
        super(webSocketsUtils, gameSessionUtils, leaderboardUtils, questionUtils, mainCtrl);
        sessionEmojis = FXCollections.observableArrayList();
        emojiImages = new ArrayList<Image>();
        String[] emojiFileNames = {"funny", "sad", "angry"};
        ClassLoader cl = getClass().getClassLoader();
        for (String fileName : emojiFileNames) {
            URL location = cl.getResource(
                    Path.of("", "client", "scenes", "emojis", fileName + ".png").toString());

            emojiImages.add(new Image(location.toString()));
        }
        usedJokers = new ArrayList<>();
    }

    /**
     * {@inheritDoc}
     */
    public void initialize(URL url, ResourceBundle res) {
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

        leaderboard.setPrefWidth(IN_GAME_LEADERBOARD_WIDTH);
        colUserName.setPrefWidth(IN_GAME_COLUSERNAME_WIDTH);
        leaderboard.setOpacity(1);

        backButton.setOpacity(1);

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
     * Checks the server periodically for players who disconnected. If so, displays text on the game screen
     */
    public void scanForDisconnect() {
        lastDisconnectIndex = -1;
        disconnectTimer = new Timer();
        disconnectTimer.scheduleAtFixedRate(new TimerTask() {

            @Override
            public void run() {
                List<Player> allRemoved = gameSessionUtils.getRemovedPlayers(sessionId);
                List<Player> newRemoved = new ArrayList<Player>();
                for (int i = lastDisconnectIndex + 1; i < allRemoved.size(); i++) {
                    newRemoved.add(allRemoved.get(i));
                }
                Platform.runLater(() -> disconnectedText(newRemoved));
                lastDisconnectIndex = allRemoved.size() - 1;
            }
        }, 0, 2000);
    }

    /**
     * Displays the player(s) who got disconnected
     *
     * @param players Players who got disconnected
     */
    public void disconnectedText(List<Player> players) {
        if (players.size() == 0) {
            removedPlayers.setOpacity(0.0);
            return;
        }
        String req = "";
        for (int i = 0; i < players.size(); i++) {
            req += players.get(i).username + ", ";
        }
        req = req.substring(0, req.length() - 2);
        removedPlayers.setText(String.format("%s" + ": DISCONNECTED...", req));
        removedPlayers.setOpacity(1.0);
    }

    /**
     * Refreshes the multiplayer player board to check whether the evaluation can start or refreshes the board to check
     * how many players want to play again.
     */
    public void refresh() {
        Timer timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                Platform.runLater(() -> {
                    try {
                        if (gameSessionUtils.getSession(sessionId).sessionStatus
                                == GameSession.SessionStatus.PAUSED) {
                            startEvaluation();
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
     * Renders the leaderboard at the start of a question and renders the rest of the general information
     *
     * @param q the question to be rendered
     */
    @Override
    public void renderGeneralInformation(Question q) {
        renderLeaderboard();
        super.renderGeneralInformation(q);
    }

    /**
     * Renders the correct answer and updates the leaderboard
     */
    @Override
    public void renderCorrectAnswer() {
        super.renderCorrectAnswer();
        renderLeaderboard();
    }

    /**
     * Resizes the leaderboard and displays the question screen attributes
     */
    @Override
    public void removeMidGameLeaderboard() {
        countdown.setOpacity(1);
        leaderboard.setPrefWidth(IN_GAME_LEADERBOARD_WIDTH);
        colUserName.setPrefWidth(IN_GAME_COLUSERNAME_WIDTH);
        super.removeMidGameLeaderboard();
    }

    /**
     * Interrupts the timer, disables the submit button, sends the user's answer for evaluation and pauses the game
     * until everyone has answered or the timer has terminated.
     */
    public void submitAnswer(boolean initiatedByTimer) {
        super.submitAnswer(initiatedByTimer);
        if (!initiatedByTimer && this.evaluation == null) return;

        //enable jokers that can be used after submitting an answer
        disableButton(decreaseTimeButton, !decreaseTimeJoker);
        disableButton(doublePointsButton, !doublePointsJoker);

        refresh();
    }

    @Override
    public void shutdown() {
        if (submitButton.isDisabled()) {
            gameSessionUtils.toggleReady(sessionId, false);
        }
        channel.unsubscribe();
        super.shutdown();
        disconnectTimer.cancel();
        lastDisconnectIndex = -1;
        jokerTimer.cancel();
        lastJokerIndex = -1;
    }

    /**
     * Register the client to receive emoji reactions from other players
     */
    public void registerForEmojiUpdates() {
        sessionEmojis.clear();
        emojiList.setItems(sessionEmojis);

        channel = this.webSocketsUtils.registerForEmojiUpdates(emoji -> {
            sessionEmojis.add(emoji);
            Platform.runLater(() -> emojiList.scrollTo(sessionEmojis.size() - 1));
        }, this.sessionId);
    }

    /**
     * Reset method that resets multiplayer only attributes.
     */
    @Override
    public void reset() {
        backButton.setOpacity(1);
        backButton.setDisable(false);
        leaderboard.setOpacity(0);
        super.reset();
    }

    @Override
    public void showPodiumScreen(long sessionId) throws InterruptedException {
        mainCtrl.showPodiumScreen(this.sessionId);

        TimeUtils timer = new TimeUtils(10L, TIMER_UPDATE_INTERVAL_MS);
        timer.setOnSucceeded((event) -> {
            Platform.runLater(() -> {
                System.out.println("111");
                mainCtrl.showEndGameScreen(sessionId, playerId);
            });
        });

        timeProgress.progressProperty().bind(timer.progressProperty());
        this.timerThread = new Thread(timer);
        this.timerThread.start();
        reset();
        channel.unsubscribe();
        disconnectTimer.cancel();
        lastDisconnectIndex = -1;
        jokerTimer.cancel();
        lastJokerIndex = -1;
    }

    /**
     * the method to deal with the joker usage in the game
     */
    public void scanForJokerUsage() {
        lastJokerIndex = -1;
        jokerTimer = new Timer();
        jokerTimer.scheduleAtFixedRate(new TimerTask() {

            @Override
            public void run() {
                Platform.runLater(() -> {
                    List<Joker> allUsed = gameSessionUtils.getUsedJoker(sessionId);
                    List<Joker> newlyUsed = new ArrayList<>();
                    for (int i = lastJokerIndex + 1; i < allUsed.size(); i++) {
                        newlyUsed.add(allUsed.get(i));
                    }
                    displayJokerUsage(newlyUsed);
                    lastJokerIndex = allUsed.size() - 1;
                });
            }
        }, 0, 2000);
    }

    /**
     * the method to display joker usage
     *
     * @param jokers a list of jokers which has been used
     */
    public void displayJokerUsage(List<Joker> jokers) {
        if (jokers.size() == 0) {
            jokerUsage.setOpacity(0.0);
            return;
        }
        String temp = "";
        for (int i = 0; i < jokers.size(); i++) {
            temp += jokers.get(i).username() + " has used " + jokers.get(i).jokerName() + ", ";
        }
        temp = temp.substring(0, temp.length() - 2);
        jokerUsage.setText(temp);
        jokerUsage.setOpacity(1.0);
    }

    @Override
    public void handleGamePodium() {
        try {
            if (gameSessionUtils.getSession(sessionId).players.size() >= 2) showPodiumScreen(sessionId);
            else back();
        } catch (BadRequestException ex) {
            setPlayerId(0);
            setSessionId(0);
            back();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}