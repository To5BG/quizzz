package commons;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import java.util.ArrayList;
import java.util.List;

import static org.apache.commons.lang3.builder.ToStringStyle.MULTI_LINE_STYLE;

public class Evaluation {
    public int points;
    public List<Long> correctAnswers;
    public Question.QuestionType type;

    public Evaluation() {
        this.type = Question.QuestionType.UNKNOWN;
        this.correctAnswers = new ArrayList<Long>();
        this.points = 0;
    }

    public Evaluation(int points, Question.QuestionType type, List<Long> correctAnswers) {
        this.type = type;
        this.correctAnswers = correctAnswers;
        this.points = points;
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
