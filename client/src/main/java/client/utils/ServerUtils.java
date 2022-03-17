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
package client.utils;

import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON;

import java.lang.reflect.Type;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;

import commons.*;
import org.glassfish.jersey.client.ClientConfig;

import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.core.GenericType;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.simp.stomp.StompFrameHandler;
import org.springframework.messaging.simp.stomp.StompHeaders;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.messaging.simp.stomp.StompSessionHandlerAdapter;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;

public class ServerUtils {

    private static final String SERVER = "http://localhost:8080/";

    /*-----------------------------------------------------------------------------------------*/
    /*--------------------------------- SESSION HANDLING --------------------------------------*/
    /*-----------------------------------------------------------------------------------------*/

    /**
     * Retrieves a game session from the DB.
     *
     * @param sessionId id of the session to retrieve
     * @return Game session with the given id
     */
    public GameSession getSession(long sessionId) {
        return ClientBuilder.newClient(new ClientConfig())
                .target(SERVER).path("api/sessions/" + sessionId)
                .request(APPLICATION_JSON)
                .accept(APPLICATION_JSON)
                .get(new GenericType<GameSession>() {
                });
    }

    /**
     * Retrieves an available game session from the DB.
     *
     * @return Available game session
     */
    public GameSession getAvailableSession() {
        return ClientBuilder.newClient(new ClientConfig())
                .target(SERVER).path("api/sessions/join")
                .request(APPLICATION_JSON)
                .accept(APPLICATION_JSON)
                .get(new GenericType<GameSession>() {
                });
    }

    /**
     * Retrieves all game sessions from the DB that are still active.
     *
     * @return All active game sessions
     */
    public List<GameSession> getSessions() {
        return ClientBuilder.newClient(new ClientConfig())
                .target(SERVER).path("api/sessions")
                .request(APPLICATION_JSON)
                .accept(APPLICATION_JSON)
                .get(new GenericType<List<GameSession>>() {
                });
    }

    /**
     * Adds a session to the DB.
     *
     * @param session GameSession object to be added
     * @return The session that has been added
     */
    public GameSession addSession(GameSession session) {
        return ClientBuilder.newClient(new ClientConfig())
                .target(SERVER).path("api/sessions")
                .request(APPLICATION_JSON)
                .accept(APPLICATION_JSON)
                .post(Entity.entity(session, APPLICATION_JSON), GameSession.class);
    }

    /**
     * Removes a session from the DB.
     *
     * @param sessionId Id of session to be removed
     * @return The response from session removal
     */
    public GameSession removeSession(long sessionId) {
        return ClientBuilder.newClient(new ClientConfig())
                .target(SERVER).path("api/sessions/" + sessionId)
                .request(APPLICATION_JSON)
                .accept(APPLICATION_JSON)
                .delete(GameSession.class);
    }

    /**
     * Updates a session status
     *
     * @param session Session to update
     * @param status  new status to be set
     * @return The updated session
     */
    public GameSession updateStatus(GameSession session, GameSession.SessionStatus status) {
        return ClientBuilder.newClient(new ClientConfig())
                .target(SERVER).path("api/sessions/" + session.id + "/status")
                .request(APPLICATION_JSON)
                .accept(APPLICATION_JSON)
                .put(Entity.entity(status, APPLICATION_JSON), GameSession.class);
    }

    /**
     * Sets and unsets a player as being ready for a multiplayer game
     *
     * @param sessionId
     * @param isReady   True iff a player must be set as ready
     * @return New count of players that are ready
     */
    public GameSession toggleReady(long sessionId, boolean isReady) {
        return ClientBuilder.newClient(new ClientConfig())
                .target(SERVER).path("api/sessions/" + sessionId + "/" + ((isReady) ? "" : "not") + "ready")
                .request(APPLICATION_JSON)
                .accept(APPLICATION_JSON)
                .get(new GenericType<GameSession>() {
                });
    }

    /**
     * Adds a player to a game session.
     *
     * @param sessionId id of the session to add the player to
     * @param player    Player object to be added
     * @return The player that has been added
     */
    public Player addPlayer(long sessionId, Player player) {
        return ClientBuilder.newClient(new ClientConfig())
                .target(SERVER).path("api/sessions/" + sessionId + "/players")
                .request(APPLICATION_JSON)
                .accept(APPLICATION_JSON)
                .post(Entity.entity(player, APPLICATION_JSON), Player.class);
    }

    /**
     * Removes a player from a game session.
     *
     * @param sessionId id of the session to remove the player from
     * @param playerId  id of player to be removed
     * @return The response from player removal
     */
    public Player removePlayer(long sessionId, long playerId) {
        return ClientBuilder.newClient(new ClientConfig())
                .target(SERVER).path("api/sessions/" + sessionId + "/players/" + playerId)
                .request(APPLICATION_JSON)
                .accept(APPLICATION_JSON)
                .delete(Player.class);
    }

    /**
     * Retrieve all players from a session in the DB.
     *
     * @param sessionId the id of the session
     * @return List of all players from a session
     */
    public List<Player> getPlayers(long sessionId) {
        return ClientBuilder.newClient(new ClientConfig())
                .target(SERVER).path("api/sessions/" + sessionId + "/players")
                .request(APPLICATION_JSON)
                .accept(APPLICATION_JSON)
                .get(new GenericType<List<Player>>() {
                });
    }

    /**
     * Updates a session's number of time jokers
     *
     * @param sessionID  Session to update
     * @param timeJokers new number of timeJokers to be set
     * @return integer for number of jokers in use now
     */
    public Integer updateTimeJokers(long sessionID, int timeJokers) {
        return ClientBuilder.newClient(new ClientConfig())
                .target(SERVER).path("api/sessions/" + sessionID + "/timeJokers/" + timeJokers)
                .request(APPLICATION_JSON)
                .accept(APPLICATION_JSON)
                .get(Integer.class);
    }

    /*-----------------------------------------------------------------------------------------*/
    /*----------------------------- ANSWER AND QUESTION HANDLING ------------------------------*/
    /*-----------------------------------------------------------------------------------------*/

    /**
     * Fetches a question from the server database
     *
     * @param sessionId Session to check
     * @return Question object related to the session with the provided id
     */
    public Question fetchOneQuestion(long sessionId) {
        return ClientBuilder.newClient(new ClientConfig())
                .target(SERVER).path("api/questions/" + sessionId)
                .request(APPLICATION_JSON)
                .accept(APPLICATION_JSON)
                .get(new GenericType<Question>() {
                });
    }

    /**
     * Submits an answer to the server database
     *
     * @param sessionId Session Id to send the answer to
     * @param answer    Answer object to be sent
     * @return Evaluation object to check the provided answers
     */
    public Evaluation submitAnswer(long sessionId, Answer answer) {
        return ClientBuilder.newClient(new ClientConfig())
                .target(SERVER).path("api/questions/" + sessionId)
                .request(APPLICATION_JSON)
                .accept(APPLICATION_JSON)
                .post(Entity.entity(answer, APPLICATION_JSON), Evaluation.class);
    }

    /**
     * Stores the player's answer with that particular player.
     *
     * @param sessionId The current session.
     * @param playerId  The player who answered.
     * @param answer    The player's answer.
     * @return The player's answer.
     */
    public Answer addPlayerAnswer(long sessionId, long playerId, Answer answer) {
        return ClientBuilder.newClient(new ClientConfig())
                .target(SERVER).path("api/sessions/" + sessionId + "/players/" + playerId)
                .request(APPLICATION_JSON)
                .accept(APPLICATION_JSON)
                .post(Entity.entity(answer, APPLICATION_JSON), Answer.class);
    }

    /**
     * Fetches the last answer of the player.
     *
     * @param sessionId The current session.
     * @param playerId  The player who answered.
     * @return The player's answer.
     */
    public Answer getPlayerAnswer(long sessionId, long playerId) {
        return ClientBuilder.newClient(new ClientConfig())
                .target(SERVER).path("api/sessions/" + sessionId + "/players/" + playerId)
                .request(APPLICATION_JSON)
                .accept(APPLICATION_JSON)
                .get(new GenericType<Answer>() {
                });
    }

    /**
     * Gets the list of positions of correct answers for a question from the server
     *
     * @param sessionId - long representing the current session
     * @return a list of integer corresponding to the positions of correct answers for a question
     */
    public List<Integer> getCorrectAnswers(long sessionId) {
        return ClientBuilder.newClient(new ClientConfig())
                .target(SERVER).path("api/questions/answers/" + sessionId)
                .request(APPLICATION_JSON)
                .accept(APPLICATION_JSON)
                .get(new GenericType<List<Integer>>() {
                });
    }

    /*----------------------------------------------------------------------------*/
    /*----------------------------- PLAYER HANDLING ------------------------------*/
    /*----------------------------------------------------------------------------*/

    /**
     * Get all player object entries from the database
     * for single mode
     * @return List of all player entries
     */
    public List<Player> getPlayerSingleScore() {
        return ClientBuilder.newClient(new ClientConfig()) //
                .target(SERVER).path("api/leaderboard/single") //
                .request(APPLICATION_JSON) //
                .accept(APPLICATION_JSON) //
                .get(new GenericType<List<Player>>() {
                });
    }

    /**
     * Get all player object entries from the database
     * for multi mode
     * @return List of all player entries
     */
    public List<Player> getPlayerMultiScore() {
        return ClientBuilder.newClient(new ClientConfig()) //
                .target(SERVER).path("api/leaderboard/multi") //
                .request(APPLICATION_JSON) //
                .accept(APPLICATION_JSON) //
                .get(new GenericType<List<Player>>() {
                });
    }

    /**
     * Retrieve a player entry from the database by ID
     *
     * @param playerId Id of player to fetch
     * @return Player entry reference, if one with the given id exists
     */
    public Player getPlayerById(long playerId) {
        return ClientBuilder.newClient(new ClientConfig()) //
                .target(SERVER).path("api/leaderboard/" + playerId) //
                .request(APPLICATION_JSON) //
                .accept(APPLICATION_JSON) //
                .get(new GenericType<Player>() {
                });
    }

    /**
     * Update a player's score in DB
     *
     * @param playerId    Id of player
     * @param points      Updated points
     * @param isBestScore Is this over the current best score for the player
     * @return Updated player DB entry reference
     */
    public Player updateScore(long playerId, int points, boolean isBestScore) {
        return ClientBuilder.newClient(new ClientConfig())
                .target(SERVER).path("api/leaderboard/" + playerId +
                        ((isBestScore) ? "/best" : "/") + "score")
                .request(APPLICATION_JSON)
                .accept(APPLICATION_JSON)
                .put(Entity.entity(points, APPLICATION_JSON), Player.class);
    }

    /**
     * Adds a player entry to the database forcibly (without an associated game session)
     *
     * @param player the player to be added
     * @return a message to show whether the adding is successful or not
     */
    public Player addPlayerForcibly(Player player) {
        return ClientBuilder.newClient(new ClientConfig()) //
                .target(SERVER).path("api/leaderboard") //
                .request(APPLICATION_JSON) //
                .accept(APPLICATION_JSON) //
                .post(Entity.entity(player, APPLICATION_JSON), Player.class);
    }

    /*-----------------------------------------------------------------------------------------*/
    /*----------------------------- PLAYER INTERACTION AND WEBSOCKET --------------------------*/
    /*-----------------------------------------------------------------------------------------*/
    private StompSession websocketServer = connect("ws://localhost:8080/websocket");

    /**
     * Create a new websocket connection
     * @param destination URL where a protocol switch to websocket can happen
     * @return The established StompSession
     */
    private StompSession connect(String destination) {
        var client = new StandardWebSocketClient();
        var stomp = new WebSocketStompClient(client);

        // Setup magic passing of objects through the network
        stomp.setMessageConverter(new MappingJackson2MessageConverter());

        try {
            return stomp.connect(destination, new StompSessionHandlerAdapter() { }).get();
        } catch (InterruptedException e) {
            System.err.println("Websocket connection timed out");
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        throw new IllegalStateException("Failed to connect to websocket server");
    }

    /**
     * Register for updates from the websocket server
     * @param callback Function to call when a message is received
     * @param type The class of the type of object that is expected
     * @param updateUrl The URL from which to receive updates
     * @param <T> The type of the object that is expected
     * @return A Subscription which can be used to unsubscribe from the updates
     */
    private <T> StompSession.Subscription registerForWebsocketUpdates(Consumer<T> callback,
                                                                      Class<T> type, String updateUrl) {
        return websocketServer.subscribe("/updates" + updateUrl, new StompFrameHandler() {
            @Override
            public Type getPayloadType(StompHeaders headers) {
                return type;
            }

            @Override
            public void handleFrame(StompHeaders headers, Object payload) {
                callback.accept((T) payload);
            }
        });
    }

    /**
     * Send a message to the websocket server
     * @param url The destination path of the message (/app is appended by the method)
     * @param body The content of the message
     * @param <T> The type of the message to send
     */
    private <T> void sendWebsocketMessage(String url, T body) {
        websocketServer.send("/app/" + url, body);
    }

    /**
     * Listen for updates regarding emojis from other players
     * @param handler The function to call with the emoji sent to the session
     * @param sessionId The ID of the session in which to listen for emojis
     */
    public StompSession.Subscription registerForEmojiUpdates(Consumer<Emoji> handler, long sessionId) {
        return registerForWebsocketUpdates(handler, Emoji.class, "/emoji/" + sessionId);
    }

    /**
     * Send an emoji to a given session
     * @param sessionId The ID of the session where the emoji is sent to
     * @param playerId The ID of the player sending the emoji
     * @param emoji The type of emoji to send
     */
    public void sendEmoji(long sessionId, long playerId, Emoji.EmojiType emoji) {
        sendWebsocketMessage("/emoji/" + sessionId + "/send/" + playerId, emoji);
    }
}