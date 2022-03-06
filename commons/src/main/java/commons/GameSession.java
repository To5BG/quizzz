package commons;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

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

    public String sessionType;
    public String sessionStatus;

    @SuppressWarnings("unused")
    private GameSession() {
        // for object mapper
    }

    public GameSession(String sessionType) {
        this(sessionType, new ArrayList<Player>(), new ArrayList<Integer>());
    }

    public GameSession(String sessionType, List<Player> players) {
        this(sessionType, players, new ArrayList<Integer>());
    }

    public GameSession(String sessionType, List<Player> players, List<Integer> expectedAnswers) {
        this.players = players;
        this.sessionType = sessionType;
        this.expectedAnswers = expectedAnswers;
        this.playersReady = 0;
        this.questionCounter = 0;
        this.sessionStatus = "started";
        if (sessionType.equals("waiting_area")) this.sessionStatus = "waiting_area";
    }

    public void setPlayerReady() {
        if (sessionType.equals("waiting_area")) {
            if (playersReady >= players.size()) return;
            playersReady++;
        } else {
            if (++playersReady != this.players.size()) return;
            updateQuestion();
            playersReady = 0;
        }
    }

    public void unsetPlayerReady() {
        if (playersReady <= 0) return;
        playersReady--;
    }

    public void addPlayer(Player player) {
        players.add(player);
    }

    public void removePlayer(Player player) {
        players.remove(player);
    }

    public void setCurrentQuestion(Question question) {
        this.currentQuestion = question;
    }

    public void updateStatus(String sessionStatus) {
        this.sessionStatus = sessionStatus;
    }

    public void updateQuestion() {
        Question q = new Question(
                "Question #" + questionCounter++,
                "N/A",
                Question.QuestionType.MULTIPLE_CHOICE
        );
        for (int i = 0; i < 3; ++i) {
            q.addAnswerOption(String.format("Option #%d", i));
        }
        System.out.println("Question updated to:");
        System.out.println(q);
        this.currentQuestion = q;
        this.expectedAnswers.clear();
        this.expectedAnswers.add(new Random().nextInt(3));
    }

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
