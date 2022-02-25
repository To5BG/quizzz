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

    @GeneratedValue(strategy = GenerationType.AUTO)
    public long id;

    public String title;
    public String consumption;
    public String imagePath;
    public String source;

    @SuppressWarnings("unused")
    public Activity() {
    }

    public Activity(String title, String consumption,String imagePath, String source) {
        this.title = title;
        this.consumption = consumption;
        this.imagePath = imagePath;
        this.source = source;
    }

    @Override
    public boolean equals(Object obj) {
        return EqualsBuilder.reflectionEquals(this, obj);
    }

    @Override
    public int hashCode() {
        return HashCodeBuilder.reflectionHashCode(this);
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, MULTI_LINE_STYLE);
    }
}