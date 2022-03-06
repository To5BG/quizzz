package server.api;

import commons.Answer;
import commons.Evaluation;
import commons.GameSession;
import commons.Question;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/questions")
public class QuestionController {
    private final SessionController sessions;

    public QuestionController(SessionController sessions) {
        this.sessions = sessions;
    }

    /**
     * Getter for current question of game session
     *
     * @param sessionId Id of session to fetch
     * @return Game session's question
     */
    @GetMapping(path = "/{sessionId}")
    public ResponseEntity<Question> getOneQuestion(@PathVariable("sessionId") long sessionId) {
        ResponseEntity<GameSession> session = sessions.getSessionById(sessionId);
        if (session.getStatusCode() == HttpStatus.BAD_REQUEST) {
            return ResponseEntity.badRequest().build();
        }

        return ResponseEntity.ok(session.getBody().currentQuestion);
    }

    /**
     * Submit an answer to the game session
     *
     * @param sessionId Id of game session to submit answer to
     * @param answer    Answer object of submission
     * @return Evaluation of answer's correctness
     */
    @PostMapping(path = "/{sessionId}")
    public ResponseEntity<Evaluation> submitAnswer(@PathVariable("sessionId") long sessionId,
                                                   @RequestBody Answer answer) {
        ResponseEntity<GameSession> session = sessions.getSessionById(sessionId);
        if (session.getStatusCode() == HttpStatus.BAD_REQUEST) {
            return ResponseEntity.badRequest().build();
        }

        GameSession s = session.getBody();
        Evaluation eval = new Evaluation((answer.answers.equals(s.expectedAnswers)) ? 1 : 0,
                s.currentQuestion.type, List.copyOf(s.expectedAnswers));

        s.setPlayerReady();
        sessions.updateSession(s);
        return ResponseEntity.ok(eval);
    }

    /**
     * Gets the list of positions of correct answers for the current question in a session
     * @param sessionId - long representing the id of a session
     * @return a list of integer corresponding to the positions of correct answers
     * for the current question in the session
     */
    @GetMapping(path = "/answers/{sessionId}")
    public ResponseEntity<List<Integer>> getCorrectAnswers(@PathVariable("sessionId") long sessionId) {
        ResponseEntity<GameSession> session = sessions.getSessionById(sessionId);
        if (session.getStatusCode() == HttpStatus.BAD_REQUEST) {
            return ResponseEntity.badRequest().build();
        }

        GameSession s = session.getBody();
        return ResponseEntity.ok(s.expectedAnswers);
    }
}
