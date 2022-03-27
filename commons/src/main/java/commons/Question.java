package commons;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import java.util.ArrayList;
import java.util.List;

import static org.apache.commons.lang3.builder.ToStringStyle.MULTI_LINE_STYLE;

public class Question {

    public final String prompt;
    public final String imagePath;
    public final QuestionType type;

    public List<String> answerOptions;
    public List<String> activityPath;

    public enum QuestionType {
        MULTIPLE_CHOICE,
        RANGE_GUESS,
        COMPARISON,
        EQUIVALENCE,
        UNKNOWN
    }

    public Question() {
        prompt = "";
        imagePath = "";
        type = QuestionType.UNKNOWN;
    }

    public Question(String prompt, String imagePath, QuestionType type) {
        this.prompt = prompt;
        this.imagePath = imagePath;
        this.type = type;
        this.answerOptions = new ArrayList<String>();
        this.activityPath = new ArrayList<String>();
    }

    /**
     * Add a new answer option to the list
     *
     * @param answerOption String representation of the answer to be added
     */
    public void addAnswerOption(String answerOption) {
        if (type != QuestionType.MULTIPLE_CHOICE && type != QuestionType.COMPARISON &&
                type != QuestionType.EQUIVALENCE) {
            throw new UnsupportedOperationException(
                    "Answer options are only allowed for Multiple Choice, comparison and equivalence type questions");
        }
        this.answerOptions.add(answerOption);
    }

    /**
     * Add an activityPath to the list
     *
     * @param activityPath Image path of the corresponding activity in string form
     */
    public void addActivityPath(String activityPath) {
        if (type != QuestionType.COMPARISON && type != QuestionType.EQUIVALENCE) {
            throw new UnsupportedOperationException(
                    "There are no extra paths for multiple choice and range questions");
        }
        this.activityPath.add(activityPath);
    }

    /**
     * Equals method
     *
     * @param obj - Object that will be compared with this
     * @return true if this and obj are equal
     */
    @Override
    public boolean equals(Object obj) {
        return EqualsBuilder.reflectionEquals(this, obj);
    }

    /**
     * Hashcode method
     *
     * @return the hashcode of the activity
     */
    @Override
    public int hashCode() {
        return HashCodeBuilder.reflectionHashCode(this);
    }

    /**
     * ToString method
     *
     * @return String containing the activity in a readable format
     */
    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, MULTI_LINE_STYLE);
    }
}
