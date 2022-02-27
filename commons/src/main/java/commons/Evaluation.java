package commons;

import java.util.ArrayList;
import java.util.List;

public class Evaluation {
    public int points;
    public List<Integer> correctAnswers;
    public Question.QuestionType type;

    public Evaluation() {
        this.type = Question.QuestionType.UNKNOWN;
        this.correctAnswers = new ArrayList<Integer>();
        this.points = 0;
    }

    public Evaluation(int points, Question.QuestionType type, List<Integer> correctAnswers) {
        this.type = type;
        this.correctAnswers = correctAnswers;
        this.points = points;
    }
}
