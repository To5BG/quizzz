package commons;

import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class QuestionGeneratorTest {

    @Test
    public void testGenerateQuestionInternalRng() {
        Pair<Question, List<Integer>> res = QuestionGenerator.generateQuestion();
        assertNotNull(res.getKey());
        assertNotNull(res.getValue());
        assertTrue(res.getValue().size() >= 1);
        assertNotEquals(Question.QuestionType.UNKNOWN, res.getKey().type);
    }

    @Test
    public void testGenerateComparison() {
        Pair<Question, List<Integer>> res = QuestionGenerator.generateQuestion(Question.QuestionType.COMPARISON);
        Question q = res.getKey();
        assertEquals("Which activity takes the most energy?", q.prompt);
        assertEquals(Question.QuestionType.COMPARISON, q.type);
        assertSame(4, q.answerOptions.size());
        assertSame(1, res.getValue().size());
    }

    @Test
    public void testGenerateMultipleChoice() {
        Pair<Question, List<Integer>> res = QuestionGenerator.generateQuestion(Question.QuestionType.MULTIPLE_CHOICE);
        Question q = res.getKey();
        assertTrue(q.prompt.startsWith("Guess how much energy the following activity takes\n"));
        assertEquals(Question.QuestionType.MULTIPLE_CHOICE, q.type);
        assertSame(4, q.answerOptions.size());
        assertSame(1, res.getValue().size());
    }

    @Test
    public void testGenerateEquivalence() {
        Pair<Question, List<Integer>> res = QuestionGenerator.generateQuestion(Question.QuestionType.EQUIVALENCE);
        Question q = res.getKey();
        assertTrue(
                q.prompt.startsWith("What could you do instead of the following activity to use the same energy?\n"));
        assertEquals(Question.QuestionType.EQUIVALENCE, q.type);
        assertSame(3, q.answerOptions.size());
        assertSame(1, res.getValue().size());
    }

    @Test
    public void testGenerateEstimation() {
        Pair<Question, List<Integer>> res = QuestionGenerator.generateQuestion(Question.QuestionType.RANGE_GUESS);
        Question q = res.getKey();
        assertTrue(q.prompt.startsWith("Guess how much Wh of energy the following activity takes\n"));
        assertEquals(Question.QuestionType.RANGE_GUESS, q.type);
        assertSame(0, q.answerOptions.size());
        assertSame(1, res.getValue().size());
    }
}
