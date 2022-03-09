package commons;

import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

public class QuestionGeneratorTest {

    class StubRandom extends Random {
        private int nextIntResult;

        public StubRandom(int retVal) {
            this.nextIntResult = retVal;
        }

        @Override
        public int nextInt() {
            return nextIntResult;
        }

        @Override
        public int nextInt(int bound) {
            return nextIntResult;
        }
    }

    @Test
    public void testGenerateQuestionInternalRng() {
        Pair<Question, List<Integer>> res = QuestionGenerator.generateQuestion();
        assertNotNull(res.getKey());
        assertNotNull(res.getValue());
        assertTrue(res.getValue().size() >= 1);
    }

    @Test
    public void testGenerateComparison() {
        StubRandom rng = new StubRandom(0);
        Pair<Question, List<Integer>> res = QuestionGenerator.generateQuestion(rng);
        Question q = res.getKey();
        assertEquals("Which activity takes the most energy?", q.prompt);
        assertEquals(Question.QuestionType.COMPARISON, q.type);
        assertSame(4, q.answerOptions.size());
        assertSame(1, res.getValue().size());
    }

    @Test
    public void testGenerateMultipleChoice() {
        StubRandom rng = new StubRandom(1);
        Pair<Question, List<Integer>> res = QuestionGenerator.generateQuestion(rng);
        Question q = res.getKey();
        assertTrue(q.prompt.startsWith("Guess how much energy the following activity takes\n"));
        assertEquals(Question.QuestionType.MULTIPLE_CHOICE, q.type);
        assertSame(4, q.answerOptions.size());
        assertSame(1, res.getValue().size());
    }

    @Test
    public void testGenerateEquivalence() {
        StubRandom rng = new StubRandom(2);
        Pair<Question, List<Integer>> res = QuestionGenerator.generateQuestion(rng);
        Question q = res.getKey();
        assertTrue(q.prompt.startsWith("What could you do instead of the following activity to use the same energy?\n"));
        assertEquals(Question.QuestionType.EQUIVALENCE, q.type);
        assertSame(3, q.answerOptions.size());
        assertSame(1, res.getValue().size());
    }

    @Test
    public void testGenerateEstimation() {
        StubRandom rng = new StubRandom(3);
        Pair<Question, List<Integer>> res = QuestionGenerator.generateQuestion(rng);
        Question q = res.getKey();
        assertTrue(q.prompt.startsWith("Guess how much Wh of energy the following activity takes\n"));
        assertEquals(Question.QuestionType.RANGE_GUESS, q.type);
        assertSame(0, q.answerOptions.size());
        assertSame(1, res.getValue().size());
    }
}
