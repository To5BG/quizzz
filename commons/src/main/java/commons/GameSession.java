package commons;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.tuple.Pair;

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

    @OneToOne(cascade = CascadeType.ALL)
    public Question currentQuestion;

    @ElementCollection
    public List<Integer> expectedAnswers;

    public int playersReady;
    public int questionCounter;

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
        PAUSED
    }

    @SuppressWarnings("unused")
    private GameSession() {
        // for object mapper
    }

    public GameSession(SessionType sessionType) {
        this(sessionType, new ArrayList<Player>(), new ArrayList<Integer>());
    }

    public GameSession(SessionType sessionType, List<Player> players) {
        this(sessionType, players, new ArrayList<Integer>());
    }

    public GameSession(SessionType sessionType, List<Player> players, List<Integer> expectedAnswers) {
        this.players = players;
        this.sessionType = sessionType;
        this.expectedAnswers = expectedAnswers;
        this.playersReady = 0;
        this.questionCounter = 1;

        this.sessionStatus = SessionStatus.STARTED;
        if (sessionType == SessionType.WAITING_AREA) this.sessionStatus = SessionStatus.WAITING_AREA;
    }

    /**
     * Called when a new player has triggered a ready event
     */
    public void setPlayerReady() {
        if (sessionType == SessionType.WAITING_AREA) {
            if (playersReady >= players.size()) return;
            playersReady++;
        } else {
            if (++playersReady != this.players.size()) return;
            updateQuestion();
        }
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
    }

    public void setCurrentQuestion(Question question) {
        this.currentQuestion = question;
    }

    public void setSessionStatus(SessionStatus sessionStatus) {
        this.sessionStatus = sessionStatus;
    }

    /**
     * Updates the question of the game session
     */
    public void updateQuestion() {
        ++questionCounter;
        Pair<Question, List<Integer>> res = QuestionGenerator.generateQuestion();
        this.currentQuestion = res.getKey();
        System.out.println("Question updated to:");
        System.out.println(this.currentQuestion);
        this.expectedAnswers.clear();
        this.expectedAnswers.addAll(res.getValue());
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
