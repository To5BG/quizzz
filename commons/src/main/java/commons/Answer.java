package commons;

import java.util.ArrayList;
import java.util.List;

public class Answer {
    public Question.QuestionType type;
    public List<Integer> answers;

    public Answer() {
        this.type = Question.QuestionType.UNKNOWN;
        answers = new ArrayList<Integer>();
    }

    public Answer(Question.QuestionType type) {
        this.type = type;
        answers = new ArrayList<Integer>();
    }

    public void addAnswer(int answer) {
        answers.add(answer);
    }
}
