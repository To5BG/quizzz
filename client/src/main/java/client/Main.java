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
import javafx.scene.Parent;
import javafx.stage.Stage;
import javafx.util.Pair;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import static com.google.inject.Guice.createInjector;

public class Main extends Application {

    private static final Injector INJECTOR = createInjector(new MyModule());
    private static final MyFXML FXML = new MyFXML(INJECTOR);

    public static void main(String[] args) throws URISyntaxException, IOException {
        launch();
    }

    @Override
    public void start(Stage primaryStage) throws IOException {

        List<Pair<? extends SceneCtrl, Parent>> pairs = new ArrayList<>(List.of(
                FXML.load(SplashCtrl.class, "client", "scenes", "SplashScreen.fxml"),
                FXML.load(MultiplayerCtrl.class, "client", "scenes", "MultiplayerSession.fxml"),
                FXML.load(RoomSelectionCtrl.class, "client","scenes","RoomSelection.fxml"),
                FXML.load(SingleplayerCtrl.class, "client", "scenes", "GameScreen.fxml"),
                FXML.load(WaitingAreaCtrl.class, "client", "scenes", "WaitingAreaScreen.fxml"),
                FXML.load(LeaderBoardCtrl.class, "client", "scenes", "Leaderboard.fxml"),
                FXML.load(WebViewCtrl.class, "client", "scenes", "WebViewScreen.fxml"),
                FXML.load(TutorialScreenCtrl.class, "client", "scenes", "TutorialScreen.fxml"),
                FXML.load(GamemodeCtrl.class, "client", "scenes", "GamemodeScreen.fxml"),
                FXML.load(TimeAttackCtrl.class, "client", "scenes", "TimeAttackScreen.fxml"),
                FXML.load(SurvivalCtrl.class, "client", "scenes", "SurvivalScreen.fxml")
        ));

        var mainCtrl = INJECTOR.getInstance(MainCtrl.class);
        mainCtrl.initialize(primaryStage, pairs);
    }
}