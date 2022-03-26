package commons;

import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.apache.commons.lang3.builder.ToStringStyle.MULTI_LINE_STYLE;

public class GameSession {
    public final static int GAME_ROUNDS = 2;

    public long id;

    public List<Player> players;
    public List<Player> removedPlayers;

    public Question currentQuestion;
    public List<Long> expectedAnswers;

    public AtomicInteger playersReady;
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
        this(sessionType, new ArrayList<Player>(), new ArrayList<Long>());
    }

    public GameSession(SessionType sessionType, List<Player> players) {
        this(sessionType, players, new ArrayList<Long>());
    }

    public GameSession(SessionType sessionType, List<Player> players, List<Long> expectedAnswers) {
        this.removedPlayers = new ArrayList<Player>();
        this.players = players;
        this.sessionType = sessionType;
        this.expectedAnswers = expectedAnswers;
        this.playersReady = new AtomicInteger(0);
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
        if (playersReady.get() >= players.size()) return;
        playersReady.incrementAndGet();
    }

    /**
     * Called when a player has triggered a non-ready event
     */
    public void unsetPlayerReady() {
        if (playersReady.get() <= 0) return;
        playersReady.decrementAndGet();
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
        if (sessionType == SessionType.WAITING_AREA) return;
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
     *
     * @return int representing the number of time jokers
     */
    public int getTimeJokers() {
        return this.timeJokers;
    }

    /**
     * Set the timeJoker to a new value
     *
     * @param timeJokers - the new value for time Joker
     */
    public void setTimeJokers(int timeJokers) {
        this.timeJokers = timeJokers;
    }

    /**
     * Returns the list of players in the game session
     *
     * @return list of players belonging to the game session
     */
    public List<Player> getPlayers() {
        return players;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null || obj.getClass() != GameSession.class) return false;
        GameSession other = (GameSession) obj;
        if (((this.currentQuestion == null) != (other.currentQuestion == null)) ||
                ((this.expectedAnswers == null) != (other.expectedAnswers == null))) {
            return false;
        }
        return this.id == other.id && this.players.equals(other.players) &&
                this.removedPlayers.equals(other.removedPlayers) &&
                (this.currentQuestion == null || this.currentQuestion.equals(other.currentQuestion)) &&
                (this.expectedAnswers == null || this.expectedAnswers.equals(other.expectedAnswers)) &&
                this.playersReady.get() == other.playersReady.get() &&
                this.questionCounter == other.questionCounter &&
                this.difficultyFactor == other.difficultyFactor &&
                this.timeJokers == other.timeJokers &&
                this.sessionType.equals(other.sessionType) &&
                this.sessionStatus.equals(other.sessionStatus);
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
