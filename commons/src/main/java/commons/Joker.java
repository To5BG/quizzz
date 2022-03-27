package commons;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import static org.apache.commons.lang3.builder.ToStringStyle.MULTI_LINE_STYLE;

public class Joker {
    public String username;
    public String jokerName;

    /**
     * the constructor for joker when no parameters are provided (for tests)
     */
    public Joker() {
        // for object mapper
        this.username = null;
        this.jokerName =null;
    }

    /**
     * the constructor for joker class
     * @param username the username of the player who has used the joker
     * @param jokerName the name of joker card which has been used
     */
    public Joker(String username, String jokerName) {
        this.username = username;
        this.jokerName = jokerName;
    }

    /**
     * the getter method for username in joker class
     * @return the name of the player who has used the joker
     */
    public String getUsername() {
        return username;
    }

    /**
     * the getter method for jokerName in the joker class
     * @return the name of the joker card which has been used
     */
    public String getJokerName() {
        return jokerName;
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
