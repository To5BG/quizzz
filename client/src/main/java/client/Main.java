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
package client;

import client.scenes.*;
import com.google.inject.Injector;
import javafx.application.Application;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URISyntaxException;

import static com.google.inject.Guice.createInjector;

public class Main extends Application {

    private static final Injector INJECTOR = createInjector(new MyModule());
    private static final MyFXML FXML = new MyFXML(INJECTOR);

    public static void main(String[] args) throws URISyntaxException, IOException {
        launch();
    }

    @Override
    public void start(Stage primaryStage) throws IOException {

        var splash = FXML.load
                (SplashCtrl.class, "client", "scenes", "SplashScreen.fxml");
        var multiplayer = FXML.load
                (MultiplayerCtrl.class, "client", "scenes", "MultiplayerSession.fxml");
        var singleplayer = FXML.load
                (SingleplayerCtrl.class, "client", "scenes", "GameScreen.fxml");
        var roomSelection = FXML.load
                (RoomSelectionCtrl.class, "client", "scenes", "RoomSelection.fxml");
        var waitingArea = FXML.load
                (WaitingAreaCtrl.class, "client", "scenes", "WaitingAreaScreen.fxml");
        var leaderboard = FXML.load
                (LeaderBoardCtrl.class, "client", "scenes", "Leaderboard.fxml");
        var podium = FXML.load
                (PodiumCtrl.class, "client", "scenes", "PodiumScreen.fxml");


        primaryStage.setOnHidden(e -> {
            try {
                waitingArea.getKey().shutdown();
            } catch (Exception exit) {
                try {
                    multiplayer.getKey().shutdown();
                } catch (Exception exit2) {
                    try {
                        singleplayer.getKey().shutdown();
                    } catch (Exception exit3) {
                    }
                }
            }
        });

        var mainCtrl = INJECTOR.getInstance(MainCtrl.class);
        mainCtrl.initialize(primaryStage, splash, multiplayer, roomSelection, waitingArea, singleplayer, leaderboard
                            , podium);
    }
}