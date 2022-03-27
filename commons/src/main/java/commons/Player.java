package commons;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import javax.persistence.*;

import static org.apache.commons.lang3.builder.ToStringStyle.MULTI_LINE_STYLE;

@Entity
public class Player {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    public long id;
    public int currentPoints;
    public int bestSingleScore;
    public int bestMultiScore;

    public String username;
    public String ans;

    @SuppressWarnings("unused")
    public Player() {
        this.currentPoints = 0;
    }

    public Player(String username, int point) {
        this.username = username;
        this.bestSingleScore = point;
        this.bestMultiScore = point;
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
     * A setter for the bestSingleScore
     *
     * @param points the point which is the best single score
     */
    public void setBestSingleScore(int points) {
        this.bestSingleScore = points;
    }

    /**
     * A setter for the bestMultiScore
     *
     * @param points the point which is the best multiMode score
     */
    public void setBestMultiScore(int points) {
        this.bestMultiScore = points;
    }

    /**
     * Getter for best single score
     */
    public Integer getBestSingleScore() {
        return this.bestSingleScore;
    }

    /**
     * Getter for best multiMode score
     *
     * @return
     */
    public Integer getBestMultiScore() {
        return this.bestMultiScore;
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

    /**
     * the method to get the username of a player
     *
     * @return the username of the player
     */
    public String getUsername() {
        return username;
    }
}
