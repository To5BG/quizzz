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

import java.util.*;

public class MultiplayerCtrl extends GameCtrl{


    @Inject
    public MultiplayerCtrl(ServerUtils server, MainCtrl mainCtrl) {
        super(server,mainCtrl);
    }

    /**
     * Refreshes the multiplayer player board to check whether the evaluation can start.
     */
    public void refresh() {
        Timer t = new Timer();
        t.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            if (server.getSession(sessionId).sessionStatus
                                    == GameSession.SessionStatus.PAUSED) {
                                startEvaluation();
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
    public void submitAnswer() {
        super.submitAnswer();
        refresh();
    }
}