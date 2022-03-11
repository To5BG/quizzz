package commons;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import javax.persistence.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.apache.commons.lang3.builder.ToStringStyle.MULTI_LINE_STYLE;

@Entity
public class Player {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    public long id;
    public int currentPoints;
    public int bestScore;

    /*
    @ManyToOne
    @JoinColumn(name = "session_id")
    private GameSession session;
    */

    public String username;
    public String ans;

    @SuppressWarnings("unused")
    private Player() {
        // for object mapper
    }

    public Player(String username, int point) {
        this.username = username;
        this.bestScore = point;
        this.currentPoints = 0;
    }

    /**
     * Sets the player's point total to the value in parameter
     *
     * @param points points to set to the player
     */
    public void setCurrentPoints(int points) {
        this.currentPoints = points;
    }

    /**
     * Sets the player's best point total to the value in parameter
     *
     * @param points points to set to the player
     */
    public void setBestPoints(int points) {
        this.bestScore = points;
    }

    /**
     * Getter method for bestScore
     *
     * @return autoboxed best score of player
     */
    public Integer getBestScore() {
        return this.bestScore;
    }

    /**
     * Getter method for current points
     *
     * @return autoboxed best score of player
     */
    public Integer getCurrentPoints() {
        return this.currentPoints;
    }

    /**
     * Converts the answer to a string.
     *
     * @param ans The player's answer.
     */
    public void setAnswer(Answer ans) {
        this.ans = ans.toString().substring(ans.toString().indexOf("["));
    }

    /**
     * Parses the answer that has been turned into a string back into and answer object.
     *
     * @return The answer in answer form.
     */
    public Answer parsedAnswer() {
        String[] splitAnswer = this.ans.split("=");
        String answer = splitAnswer[1].substring(1, splitAnswer[1].indexOf("]"));
        List<Integer> answers;
        if (answer.equals("")) answers = new ArrayList<>();
        else {
            answers = new ArrayList<>(
                    Arrays.stream(answer.split(", "))
                            .map(Integer::valueOf)
                            .collect(Collectors.toList()));
        }
        Question.QuestionType type;
        switch (splitAnswer[2].substring(0, splitAnswer[2].indexOf("]"))) {
            case "MULTIPLE_CHOICE" -> type = Question.QuestionType.MULTIPLE_CHOICE;
            default -> type = Question.QuestionType.UNKNOWN;
        }
        return new Answer(answers, type);
    }

    /**
     * the method to judge whether two players are the same
     *
     * @param obj another player to be compared with
     * @return a boolean value which represents the result of the comparison
     */
    @Override
    public boolean equals(Object obj) {
        return EqualsBuilder.reflectionEquals(this, obj);
    }

    /**
     * the hashcode method to generate a hashcode for each player
     *
     * @return a hashcode for the player input
     */
    @Override
    public int hashCode() {
        return HashCodeBuilder.reflectionHashCode(this);
    }

    /**
     * the method to convert the information about a player to a sentence
     *
     * @return a string which contains all information about this player
     */
    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, MULTI_LINE_STYLE);
    }

}
