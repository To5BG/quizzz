package commons;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import static org.apache.commons.lang3.builder.ToStringStyle.MULTI_LINE_STYLE;

public class Emoji {
    public enum EmojiType {
        FUNNY,
        SAD,
        ANGRY,
        UNKNOWN
    }

    public String username;
    public EmojiType emoji;

    public Emoji() {
        // for object mapper
        this.username = null;
        this.emoji = EmojiType.UNKNOWN;
    }

    public Emoji(String username, EmojiType type) {
        this.username = username;
        this.emoji = type;
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
