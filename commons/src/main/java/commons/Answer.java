package commons;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import java.util.ArrayList;
import java.util.List;

import static org.apache.commons.lang3.builder.ToStringStyle.MULTI_LINE_STYLE;

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

    /**
     * Constructor to create an answer when the answers are known.
     *
     * @param answers   The given answer.
     * @param type      The question type.
     */
    public Answer(List<Integer> answers, Question.QuestionType type) {
        this.type = type;
        this.answers = answers;
    }

    public void addAnswer(int answer) {
        answers.add(answer);
    }

    /**
     * Equals method
     * @param obj - Object that will be compared with this
     * @return true if this and obj are equal
     */
    @Override
    public boolean equals(Object obj) {
        return EqualsBuilder.reflectionEquals(this, obj);
    }

    /**
     * Hashcode method
     * @return the hashcode of the activity
     */
    @Override
    public int hashCode() {
        return HashCodeBuilder.reflectionHashCode(this);
    }

    /**
     * ToString method
     * @return String containing the activity in a readable format
     */
    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, MULTI_LINE_STYLE);
    }
}
