package server.service;

import commons.Activity;
import commons.Question;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import server.api.ActivityController;
import server.api.TestActivityRepository;

import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

public class QuestionGeneratorTest {

    private ActivityController ctrl;
    private TestActivityRepository repo;

    @BeforeEach
    public void setup() {
        repo = new TestActivityRepository();
        repo.save(new Activity("test",42L,"test","test"));
        repo.save(new Activity("test2",43L,"test2","test2"));
        repo.save(new Activity("test3",44L,"test3","test3"));
        repo.save(new Activity("test4",45L,"test4","test4"));
        ctrl = new ActivityController(new Random(), repo);
    }

    @Test
    public void testGenerateQuestionInternalRng() {
        double difficulty = 1;
        Pair<Question, List<Long>> res = QuestionGenerator.generateQuestion(difficulty,ctrl);
        assertNotNull(res.getKey());
        assertNotNull(res.getValue());
        assertTrue(res.getValue().size() >= 1);
        assertNotEquals(Question.QuestionType.UNKNOWN, res.getKey().type);
    }

    @Test
    public void testGenerateComparison() {
        double difficulty = 1;
        Pair<Question, List<Long>> res =
                QuestionGenerator.generateTypeQuestion(Question.QuestionType.COMPARISON, difficulty, ctrl);
        assertNotNull(res);
        Question q = res.getKey();
        assertEquals("Which activity takes the most energy?", q.prompt);
        assertEquals(Question.QuestionType.COMPARISON, q.type);
        assertSame(4, q.answerOptions.size());
        assertSame(1, res.getValue().size());
    }

    @Test
    public void testGenerateMultipleChoice() {
        double difficulty = 1;
        Pair<Question, List<Long>> res =
                QuestionGenerator.generateTypeQuestion(Question.QuestionType.MULTIPLE_CHOICE, difficulty, ctrl);
        assertNotNull(res);
        Question q = res.getKey();
        assertTrue(q.prompt.startsWith("Guess how much energy the following activity takes\n"));
        assertEquals(Question.QuestionType.MULTIPLE_CHOICE, q.type);
        assertSame(4, q.answerOptions.size());
        assertSame(1, res.getValue().size());
    }

    @Test
    public void testGenerateEquivalence() {
        double difficulty = 1;
        Pair<Question, List<Long>> res =
                QuestionGenerator.generateTypeQuestion(Question.QuestionType.EQUIVALENCE, difficulty, ctrl);
        assertNotNull(res);
        Question q = res.getKey();
        assertTrue(
                q.prompt.startsWith("What could you do instead of the following activity to use the same energy?\n"));
        assertEquals(Question.QuestionType.EQUIVALENCE, q.type);
        assertSame(3, q.answerOptions.size());
        assertSame(1, res.getValue().size());
    }

    @Test
    public void testGenerateEstimation() {
        double difficulty = 1;
        Pair<Question, List<Long>> res =
                QuestionGenerator.generateTypeQuestion(Question.QuestionType.RANGE_GUESS, difficulty, ctrl);
        assertNotNull(res);
        Question q = res.getKey();
        assertTrue(q.prompt.startsWith("Guess how much Wh of energy the following activity takes\n"));
        assertEquals(Question.QuestionType.RANGE_GUESS, q.type);
        assertSame(0, q.answerOptions.size());
        assertSame(1, res.getValue().size());
    }
}
