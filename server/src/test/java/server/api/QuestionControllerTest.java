package server.api;

import commons.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

public class QuestionControllerTest {
    private QuestionController sut;
    private TestGameSessionRepository repo;
    private SessionController session;

    @BeforeEach
    public void setup() {
        repo = new TestGameSessionRepository();
        session = new SessionController(new Random(), repo, "test");
        ResponseEntity<GameSession> cur = session.addSession(
                new GameSession(GameSession.SessionType.MULTIPLAYER, List.of(new Player("test",0))));
        sut = new QuestionController(session);
    }

    @Test
    public void testGetQuestionNoSession() {
        ResponseEntity<Question> q = sut.getOneQuestion(42L);
        assertEquals(HttpStatus.BAD_REQUEST, q.getStatusCode());
    }

    @Test
    public void testGetQuestion() {
        GameSession s = session.getAllSessions().get(0);
        ResponseEntity<Question> resp = sut.getOneQuestion(s.id);
        assertEquals(HttpStatus.OK, resp.getStatusCode());
        Question q = resp.getBody();
        Question serverQuestion = session.getAllSessions().get(0).currentQuestion;

        assertEquals(serverQuestion, q);
    }

    @Test
    public void submitAnswerNoSessionTest() {
        ResponseEntity<Evaluation> resp = sut.submitAnswer(42L,
                new Answer(Question.QuestionType.MULTIPLE_CHOICE));

        assertEquals(HttpStatus.BAD_REQUEST, resp.getStatusCode());
    }

    @Test
    public void submitAnswerTest() {
        GameSession s = session.getAllSessions().get(0);
        System.out.println(s);
        List<Integer> expectedAnswers = List.copyOf(s.expectedAnswers);

        ResponseEntity<Evaluation> resp = sut.submitAnswer(s.id,
                new Answer(Question.QuestionType.MULTIPLE_CHOICE));

        Evaluation eval = resp.getBody();

        assertEquals(HttpStatus.OK, resp.getStatusCode());
        assertEquals(expectedAnswers, eval.correctAnswers);
        assertEquals(Question.QuestionType.MULTIPLE_CHOICE, eval.type);
    }

    @Test
    public void testGetAnswers() {
        GameSession s = session.getAllSessions().get(0);
        ResponseEntity<List<Integer>> resp = sut.getCorrectAnswers(s.id);
        assertEquals(HttpStatus.OK, resp.getStatusCode());
        List<Integer> list = resp.getBody();
        List<Integer> answers = session.getAllSessions().get(0).expectedAnswers;
        assertEquals(answers, list);
    }
}
