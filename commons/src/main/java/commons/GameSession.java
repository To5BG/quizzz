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

    @OneToMany(cascade = CascadeType.ALL)
    public List<Player> removedPlayers;

    @OneToOne(cascade = CascadeType.ALL)
    public Question currentQuestion;

    @ElementCollection
    public List<Integer> expectedAnswers;

    public int playersReady;
    public int questionCounter;
    public int difficultyFactor;
    public int timeJokers;

    public SessionType sessionType;
    public enum SessionType {
        WAITING_AREA,
        MULTIPLAYER,
        SINGLEPLAYER
    }

    public SessionStatus sessionStatus;
    public enum SessionStatus {
        WAITING_AREA,
        TRANSFERRING,
        ONGOING,
        STARTED,
        PAUSED,
        PLAY_AGAIN
    }

    @SuppressWarnings("unused")
    public GameSession() {
        // for object mapper
    }

    public GameSession(SessionType sessionType) {
        this(sessionType, new ArrayList<Player>(), new ArrayList<Integer>());
    }

    public GameSession(SessionType sessionType, List<Player> players) {
        this(sessionType, players, new ArrayList<Integer>());
    }

    public GameSession(SessionType sessionType, List<Player> players, List<Integer> expectedAnswers) {
        this.removedPlayers = new ArrayList<Player>();
        this.players = players;
        this.sessionType = sessionType;
        this.expectedAnswers = expectedAnswers;
        this.playersReady = 0;
        this.questionCounter = 0;
        this.difficultyFactor = 1;
        this.timeJokers = 0;

        this.sessionStatus = SessionStatus.STARTED;
        if (sessionType == SessionType.WAITING_AREA) this.sessionStatus = SessionStatus.WAITING_AREA;
    }

    /**
     * Called when a new player has triggered a ready event
     */
    public void setPlayerReady() {
        if (playersReady >= players.size()) return;
        playersReady++;
    }

    /**
     * Called when a player has triggered a non-ready event
     */
    public void unsetPlayerReady() {
        if (playersReady <= 0) return;
        playersReady--;
    }

    /**
     * Adds a player to the list of players
     *
     * @param player Player to be added
     */
    public void addPlayer(Player player) {
        players.add(player);
    }

    /**
     * Removes a player from the list of players
     *
     * @param player Player to be removed
     */
    public void removePlayer(Player player) {
        players.remove(player);
        if(sessionType == SessionType.WAITING_AREA) return;
        removedPlayers.add(player);
    }

    public void setCurrentQuestion(Question question) {
        this.currentQuestion = question;
    }

    public void setSessionStatus(SessionStatus sessionStatus) {
        this.sessionStatus = sessionStatus;
    }

    /**
     * Resets the questionCounter to zero.
     */
    public void resetQuestionCounter() {
        this.questionCounter = 0;
    }

    /**
     * Get the number of time jokers used in this round
     * @return int representing the number of time jokers
     */
    public int getTimeJokers() {
        return this.timeJokers;
    }

    /**
     * Set the timeJoker to a new value
     * @param timeJokers - the new value for time Joker
     */
    public void setTimeJokers(int timeJokers) {
        this.timeJokers = timeJokers;
    }

    /**
     * Returns the list of players in the game session
     * @return list of players belonging to the game session
     */
    public List<Player> getPlayers() {
        return players;
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
