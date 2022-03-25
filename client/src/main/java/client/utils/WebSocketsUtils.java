package client.utils;

import commons.Emoji;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.simp.stomp.StompFrameHandler;
import org.springframework.messaging.simp.stomp.StompHeaders;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.messaging.simp.stomp.StompSessionHandlerAdapter;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;

import java.lang.reflect.Type;
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;


public class WebSocketsUtils {

    private StompSession websocketServer;

    /**
     * Public method for accessing the connect method.
     * Creates a new StompSession associated to the websocket.
     *
     * @param destination URL to connect to
     */
    public void updateConnection(String destination) {
        websocketServer = connect(destination.replaceFirst("http", "ws").concat("websocket"));
    }

    /**
     * Create a new websocket connection
     *
     * @param destination URL where a protocol switch to websocket can happen
     * @return The established StompSession
     */
    private StompSession connect(String destination) {
        var client = new StandardWebSocketClient();
        var stomp = new WebSocketStompClient(client);

        // Setup magic passing of objects through the network
        stomp.setMessageConverter(new MappingJackson2MessageConverter());

        try {
            return stomp.connect(destination, new StompSessionHandlerAdapter() {
            }).get();
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
     *
     * @param callback  Function to call when a message is received
     * @param type      The class of the type of object that is expected
     * @param updateUrl The URL from which to receive updates
     * @param <T>       The type of the object that is expected
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
     * Listen for updates regarding emojis from other players
     *
     * @param handler   The function to call with the emoji sent to the session
     * @param sessionId The ID of the session in which to listen for emojis
     */
    public StompSession.Subscription registerForEmojiUpdates(Consumer<Emoji> handler, long sessionId) {
        return registerForWebsocketUpdates(handler, Emoji.class, "/emoji/" + sessionId);
    }

    /**
     * Send a message to the websocket server
     *
     * @param url  The destination path of the message (/app is appended by the method)
     * @param body The content of the message
     * @param <T>  The type of the message to send
     */
    private <T> void sendWebsocketMessage(String url, T body) {
        websocketServer.send("/app/" + url, body);
    }


    /**
     * Send an emoji to a given session
     *
     * @param sessionId The ID of the session where the emoji is sent to
     * @param playerId  The ID of the player sending the emoji
     * @param emoji     The type of emoji to send
     */
    public void sendEmoji(long sessionId, long playerId, Emoji.EmojiType emoji) {
        sendWebsocketMessage("/emoji/" + sessionId + "/send/" + playerId, emoji);
    }
}
