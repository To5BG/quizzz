package client.utils;

import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON;

import java.util.List;

import commons.*;
import org.glassfish.jersey.client.ClientConfig;

import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.core.GenericType;

public class QuestionUtils {

    private static final String SERVER = "http://localhost:8080/";

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
}
