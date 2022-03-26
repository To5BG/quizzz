package server.api;

import commons.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/questions")
public class QuestionController {
    private final SessionController sessions;
    private final LeaderboardController leaderboard;

    public QuestionController(SessionController sessions, LeaderboardController leaderboard) {
        this.sessions = sessions;
        this.leaderboard = leaderboard;
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

    private int calculateAnswerPoints(Evaluation eval, Answer ans, double difficultyFactor) {
        int temppoints;
        switch (eval.type) {
            case MULTIPLE_CHOICE:
            case COMPARISON:
            case EQUIVALENCE:
                temppoints = (int) ((80 * eval.points * ans.timeFactor) +
                        (20 * eval.points));
                break;
            case RANGE_GUESS:
                long givenAnswer;
                long actualAnswer = eval.correctAnswers.get(0);
                try {
                    givenAnswer = ans.answers.get(0);
                } catch (NumberFormatException ex) {
                    temppoints = 0;
                    break;
                }
                // diff : magnitude of difference between the entered answer and the actual answer
                double diff = Math.abs(givenAnswer - actualAnswer);
                if (diff == 0) {
                    temppoints = (int) (60 * eval.points * ans.timeFactor) + 40;
                } else {
                    if (diff > actualAnswer) diff = actualAnswer;
                    // timeDependent : the part of the points that depends on time and accuracy - between 0 and 90
                    double timeDependent = (90 - 90 * (diff * difficultyFactor / actualAnswer)) * ans.timeFactor;
                    // constant : the part of the points that depends on accuracy and not time - between 0 and 10
                    double constant = (diff < actualAnswer) ? 10 - 10 * (diff * difficultyFactor / actualAnswer) : 0;
                    temppoints = (int) (timeDependent + constant);
                    if (temppoints <= 0) temppoints = 0;
                }
                break;
            default:
                throw new UnsupportedOperationException("Unsupported question type when parsing answer");
        }

        return temppoints;
    }

    /**
     * Submit an answer to the game session
     *
     * @param sessionId Id of game session to submit answer to
     * @param answer    Answer object of submission
     * @return Evaluation of answer's correctness
     */
    @PostMapping(path = "/{sessionId}/{playerId}")
    public ResponseEntity<Evaluation> submitAnswer(@PathVariable("sessionId") long sessionId,
                                                   @PathVariable("playerId") long playerId,
                                                   @RequestBody Answer answer) {
        ResponseEntity<GameSession> session = sessions.getSessionById(sessionId);
        if (session.getStatusCode() == HttpStatus.BAD_REQUEST) {
            return ResponseEntity.badRequest().build();
        }

        GameSession s = session.getBody();
        Player player = s.getPlayers().stream().filter(p -> p.id == playerId).findFirst().orElse(null);

        if (player == null) {
            return ResponseEntity.badRequest().build();
        }

        Evaluation eval = new Evaluation((answer.answers.equals(s.expectedAnswers)) ? 1 : 0,
                s.currentQuestion.type, List.copyOf(s.expectedAnswers));

        Evaluation actual = new Evaluation(calculateAnswerPoints(eval, answer, s.difficultyFactor),
                eval.type, eval.correctAnswers);

        /*
        TODO: double points calculated here based on player who submits the answer
        this can be implemented nicely, once the relaying of jokers to other players is implemented, since the server
        will already have knowledge of who has what jokers active, for know this break the double points joker
        */

        player.currentPoints += actual.points;

        return ResponseEntity.ok(actual);
    }

    /**
     * Gets the list of positions of correct answers for the current question in a session
     * @param sessionId - long representing the id of a session
     * @return a list of integer corresponding to the positions of correct answers
     * for the current question in the session
     */
    @GetMapping(path = "/answers/{sessionId}")
    public ResponseEntity<List<Long>> getCorrectAnswers(@PathVariable("sessionId") long sessionId) {
        ResponseEntity<GameSession> session = sessions.getSessionById(sessionId);
        if (session.getStatusCode() == HttpStatus.BAD_REQUEST) {
            return ResponseEntity.badRequest().build();
        }

        GameSession s = session.getBody();
        return ResponseEntity.ok(s.expectedAnswers);
    }
}
