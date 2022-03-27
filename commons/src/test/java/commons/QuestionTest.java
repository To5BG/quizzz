package commons;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class QuestionTest {
    @Test
    public void testEmptyConstructor() {
        Question q = new Question();
        assertEquals("", q.prompt);
        assertEquals("", q.imagePath);
        assertEquals(Question.QuestionType.UNKNOWN, q.type);
        assertNull(q.answerOptions);
    }

    @Test
    public void testConstructor() {
        Question q = new Question("Question 1", "test.png",
                Question.QuestionType.MULTIPLE_CHOICE);
        assertEquals("Question 1", q.prompt);
        assertEquals("test.png", q.imagePath);
        assertEquals(Question.QuestionType.MULTIPLE_CHOICE, q.type);
        assertNotNull(q.answerOptions);
    }

    @Test
    public void testAddAnswerOptionMultiChoice() {
        Question q = new Question("Question 1", "test.png",
                Question.QuestionType.MULTIPLE_CHOICE);

        assertDoesNotThrow(() -> q.addAnswerOption("opt1"));
        assertSame(1, q.answerOptions.size());
        assertEquals("opt1", q.answerOptions.get(0));
    }

    @Test
    public void testAddAnswerOptionBad() {
        Question q = new Question("Question 1", "test.png",
                Question.QuestionType.RANGE_GUESS);

        assertThrows(UnsupportedOperationException.class, () -> q.addAnswerOption("opt1"));
        assertSame(0, q.answerOptions.size());
    }

    @Test
    public void testAddActivityPathEquivalence() {
        Activity activity = new Activity("title1", 1000L, "path1", "source1");

        Question q = new Question("Question 1", "test.png",
                Question.QuestionType.EQUIVALENCE);

        assertDoesNotThrow(() -> q.addActivityPath(activity.image_path));
        assertSame("path1", q.activityPath.get(0));
    }

    @Test
    public void testAddActivityPathBad() {
        Question q = new Question("Question 1", "test.png",
                Question.QuestionType.RANGE_GUESS);

        assertThrows(UnsupportedOperationException.class, () -> q.addActivityPath("path1"));
        assertSame(0, q.activityPath.size());
    }

    @Test
    public void testEquals() {
        Question q1 = new Question("Question 1", "test.png",
                Question.QuestionType.RANGE_GUESS);
        Question q2 = new Question("Question 1", "test.png",
                Question.QuestionType.RANGE_GUESS);
        Question q3 = new Question("Question 2", "other.png",
                Question.QuestionType.MULTIPLE_CHOICE);

        assertEquals(q1, q1);
        assertEquals(q1, q2);
        assertNotEquals(q3, q1);
    }

    @Test
    public void testHashCode() {
        Question q1 = new Question("Question 1", "test.png",
                Question.QuestionType.RANGE_GUESS);
        Question q2 = new Question("Question 1", "test.png",
                Question.QuestionType.RANGE_GUESS);

        assertEquals(q1.hashCode(), q1.hashCode());
        assertEquals(q1.hashCode(), q2.hashCode());
    }

    @Test
    public void testToString() {
        Question q1 = new Question("Question 1", "test.png",
                Question.QuestionType.RANGE_GUESS);
        String result = q1.toString();

        assertTrue(result.contains(Question.class.getSimpleName()));
        assertTrue(result.contains("prompt=Question 1"));
        assertTrue(result.contains("imagePath=test.png"));
        assertTrue(result.contains("type=RANGE_GUESS"));
        assertTrue(result.contains("answerOptions=[]"));
    }
}
