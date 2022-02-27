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

    @GetMapping(path = "/{sessionId}")
    public ResponseEntity<Question> getOneQuestion(@PathVariable("sessionId") long sessionId) {
        ResponseEntity<GameSession> session = sessions.getSessionById(sessionId);
        if (session.getStatusCode() == HttpStatus.BAD_REQUEST) {
            return ResponseEntity.badRequest().build();
        }

        return ResponseEntity.ok(session.getBody().currentQuestion);
    }

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

        s.playerAnswered();

        sessions.updateSession(s);

        return ResponseEntity.ok(eval);
    }
}
