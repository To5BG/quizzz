package server.api;

import commons.GameSession;
import commons.Question;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import server.database.SessionRepository;

@RestController
@RequestMapping("api/questions")
public class QuestionController {
    public int questionCounter = 0;
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
}
