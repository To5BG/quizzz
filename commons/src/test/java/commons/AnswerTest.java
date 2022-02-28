package commons;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class AnswerTest {
    @Test
    public void testEmptyConstructor() {
        Answer ans = new Answer();
        assertEquals(Question.QuestionType.UNKNOWN, ans.type);
        assertNotNull(ans.answers);
    }

    @Test
    public void testConstructor() {
        Answer ans = new Answer(Question.QuestionType.MULTIPLE_CHOICE);
        assertEquals(Question.QuestionType.MULTIPLE_CHOICE, ans.type);
        assertNotNull(ans.answers);
    }

    @Test
    public void testAddAnswer() {
        Answer ans = new Answer(Question.QuestionType.MULTIPLE_CHOICE);
        assertSame(0, ans.answers.size());
        ans.addAnswer(1);
        assertSame(1, ans.answers.size());
        assertEquals(1, ans.answers.get(0));
    }

    @Test
    public void testEquals() {
        Answer ans1 = new Answer(Question.QuestionType.MULTIPLE_CHOICE);
        ans1.addAnswer(1);
        Answer ans2 = new Answer(Question.QuestionType.MULTIPLE_CHOICE);
        ans2.addAnswer(1);
        Answer ans3 = new Answer(Question.QuestionType.TRUE_FALSE);

        assertEquals(ans1, ans1);
        assertEquals(ans1, ans2);
        assertNotEquals(ans3, ans1);
    }

    @Test
    public void testHashCode() {
        Answer ans1 = new Answer(Question.QuestionType.MULTIPLE_CHOICE);
        ans1.addAnswer(1);
        Answer ans2 = new Answer(Question.QuestionType.MULTIPLE_CHOICE);
        ans2.addAnswer(1);

        assertEquals(ans1.hashCode(), ans1.hashCode());
        assertEquals(ans1.hashCode(), ans2.hashCode());
    }

    @Test
    public void testToString() {
        Answer ans1 = new Answer(Question.QuestionType.MULTIPLE_CHOICE);
        ans1.addAnswer(1);
        String result = ans1.toString();

        assertTrue(result.contains(Answer.class.getSimpleName()));
        assertTrue(result.contains("type=MULTIPLE_CHOICE"));
        assertTrue(result.contains("answers=[1]"));
    }
}
