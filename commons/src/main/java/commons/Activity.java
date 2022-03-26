package commons;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import static org.apache.commons.lang3.builder.ToStringStyle.MULTI_LINE_STYLE;

@Entity
public class Activity {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    public long id;

    public String title;
    public long consumption_in_wh;
    public String image_path;
    public String source;

    /**
     * Empty constructor
     */
    @SuppressWarnings("unused")
    public Activity() {
    }

    /**
     * Constructor method for Activity
     * @param title - String representing the name of the activity
     * @param consumption_in_wh - String representing the consumption in Wh of the activity
     * @param image_path - String representing the path to the image of the activity
     * @param source  - String representing the source URL where the consumption information was taken from
     */
    public Activity(String title, long consumption_in_wh,String image_path, String source) {
        this.title = title;
        this.consumption_in_wh = consumption_in_wh;
        this.image_path = image_path;
        this.source = source;
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