package server.api;

import commons.Question;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/questions")
public class QuestionController {
    public int questionCounter = 0;

    @GetMapping(path = {"", "/"})
    public ResponseEntity<Question> getOneQuestion() {
        Question q = new Question(
            "Question #" + questionCounter++,
            "N/A",
            Question.QuestionType.MULTIPLE_CHOICE
        );
        for (int i = 0; i < 3; ++i) {
            q.addAnswerOption(String.format("Option #%d", i));
        }
        System.out.println(q);
        return ResponseEntity.ok(q);
    }
}
