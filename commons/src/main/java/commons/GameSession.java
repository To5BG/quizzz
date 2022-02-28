package commons;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

import static org.apache.commons.lang3.builder.ToStringStyle.MULTI_LINE_STYLE;

@Entity
public class GameSession {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    public long id;

    @OneToMany(cascade = CascadeType.ALL)
    public List<Player> players;

    public String sessionType;
    public String sessionStatus;
    public int playersReady;

    @SuppressWarnings("unused")
    private GameSession() {
        // for object mapper
    }

    public GameSession(String sessionType) {
        this(sessionType, new ArrayList<Player>());
    }

    public GameSession(String sessionType, List<Player> players) {
        this.players = players;
        this.sessionType = sessionType;
        this.playersReady = 0;
        this.sessionStatus = "started";
        if (sessionType.equals("waiting_area")) this.sessionStatus = "waiting_area";
    }

    public void addPlayer(Player player) {
        players.add(player);
    }

    public void setPlayerReady() {
        if (playersReady >= players.size()) return;
        playersReady++;
    }

    public void unsetPlayerReady() {
        if (playersReady <= 0) return;
        playersReady--;
    }

    public void removePlayer(Player player) {
        players.remove(player);
    }

    public void updateStatus(String sessionStatus) {
        this.sessionStatus = sessionStatus;
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
