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

import com.google.inject.Inject;
import client.utils.ServerUtils;
import commons.*;
import javafx.application.Platform;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.util.Callback;
import org.springframework.messaging.simp.stomp.StompSession;

import java.net.URL;
import java.nio.file.Path;
import java.util.*;

public class MultiplayerCtrl extends GameCtrl {

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

    private final ObservableList<Emoji> sessionEmojis;
    private final List<Image> emojiImages;
    private StompSession.Subscription channel;

    @Inject
    public MultiplayerCtrl(ServerUtils server, MainCtrl mainCtrl) {
        super(server, mainCtrl);
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
     * Refreshes the multiplayer player board to check whether the evaluation can start.
     */
    public void refresh() {
        Timer timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            if (server.getSession(sessionId).sessionStatus
                                    == GameSession.SessionStatus.PAUSED) {
                                startEvaluation(bestMultiScore);
                                cancel();
                            }
                        } catch (Exception e) {
                            cancel();
                        }
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
     * Interrupts the timer, disables the submit button, sends the user's answer for evaluation and pauses the game
     * until everyone has answered or the timer has terminated.
     */
    public void submitAnswer(boolean initiatedByTimer) {
        super.submitAnswer(initiatedByTimer);
        if (!initiatedByTimer && this.evaluation == null) return;

        //enable jokers that can be used after submitting an answer
        if(decreaseTimeJoker) {
            disableButton(decreaseTimeButton, false);
        }
        if(doublePointsJoker) {
            disableButton(doublePointsButton, false);
        }

        refresh();
    }

    @Override
    public void shutdown() {
        channel.unsubscribe();
        super.shutdown();
    }

    /**
     * Register the client to receive emoji reactions from other players
     */
    public void registerForEmojiUpdates() {
        sessionEmojis.clear();
        emojiList.setItems(sessionEmojis);

        channel = this.server.registerForEmojiUpdates(emoji -> {
            System.out.println("Emoji received for the current room: " + emoji);
            sessionEmojis.add(emoji);
            Platform.runLater(() -> emojiList.scrollTo(sessionEmojis.size() - 1));
        }, this.sessionId);
    }

    @Override
    public void updateScore(long playerId, int points, boolean isBestScore) {
        server.updateMultiScore(playerId, points, isBestScore);
    }
}
