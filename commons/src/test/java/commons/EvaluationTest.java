package commons;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class EvaluationTest {
    @Test
    public void testEmptyConstructor() {
        Evaluation eval = new Evaluation();
        assertEquals(Question.QuestionType.UNKNOWN, eval.type);
        assertNotNull(eval.correctAnswers);
        assertEquals(0, eval.points);
    }

    @Test
    public void testConstructor() {
        List<Integer> correctAnswers = List.of(1,2,3,4);
        Evaluation eval = new Evaluation(10, Question.QuestionType.MULTIPLE_CHOICE, correctAnswers);
        assertEquals(10, eval.points);
        assertEquals(Question.QuestionType.MULTIPLE_CHOICE, eval.type);
        assertEquals(correctAnswers, eval.correctAnswers);
    }

    @Test
    public void testEquals() {
        List<Integer> correctAnswers = List.of(1,2,3,4);
        Evaluation eval1 = new Evaluation(10, Question.QuestionType.MULTIPLE_CHOICE, correctAnswers);
        Evaluation eval2 = new Evaluation(10, Question.QuestionType.MULTIPLE_CHOICE, correctAnswers);
        Evaluation eval3 = new Evaluation(20, Question.QuestionType.TRUE_FALSE, List.of(1));

        assertEquals(eval1, eval1);
        assertEquals(eval1, eval2);
        assertNotEquals(eval3, eval1);
    }

    @Test
    public void testHashCode() {
        List<Integer> correctAnswers = List.of(1,2,3,4);
        Evaluation eval1 = new Evaluation(10, Question.QuestionType.MULTIPLE_CHOICE, correctAnswers);
        Evaluation eval2 = new Evaluation(10, Question.QuestionType.MULTIPLE_CHOICE, correctAnswers);

        assertEquals(eval1.hashCode(), eval1.hashCode());
        assertEquals(eval1.hashCode(), eval2.hashCode());
    }

    @Test
    public void testToString() {
        List<Integer> correctAnswers = List.of(1,2,3,4);
        Evaluation eval1 = new Evaluation(10, Question.QuestionType.MULTIPLE_CHOICE, correctAnswers);
        String result = eval1.toString();

        assertTrue(result.contains(Evaluation.class.getSimpleName()));
        assertTrue(result.contains("points=10"));
        assertTrue(result.contains("type=MULTIPLE_CHOICE"));
        assertTrue(result.contains("correctAnswers=[1, 2, 3, 4]"));
    }
}
