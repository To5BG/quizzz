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

import static com.google.inject.Guice.createInjector;

import java.io.IOException;
import java.net.URISyntaxException;


import com.google.inject.Injector;

import client.scenes.MultiplayerCtrl;
import client.scenes.MainCtrl;
import client.scenes.SplashCtrl;
import client.scenes.GameCtrl;
import client.scenes.WaitingAreaCtrl;
import javafx.application.Application;
import javafx.stage.Stage;

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
        var game = FXML.load
                (GameCtrl.class, "client", "scenes", "GameScreen.fxml");
        var waitingArea = FXML.load(
                WaitingAreaCtrl.class, "client", "scenes", "WaitingAreaScreen.fxml");

        primaryStage.setOnHidden(e -> {
            try {
                waitingArea.getKey().shutdown();
            } catch (Exception exit) {
                try {
                    multiplayer.getKey().shutdown();
                } catch (Exception exit2) {
                    game.getKey().shutdown();
                }
            }
        });

        var mainCtrl = INJECTOR.getInstance(MainCtrl.class);
        mainCtrl.initialize(primaryStage, splash, multiplayer, waitingArea, game);
    }
}