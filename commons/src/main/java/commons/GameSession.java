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

    public int questionCounter = 0;
    public int answerCounter = 0;

    @SuppressWarnings("unused")
    private GameSession() {
        // for object mapper
        this.expectedAnswers = new ArrayList<Integer>();
    }

    public GameSession(List<Player> players) {
        this.players = players;
        this.expectedAnswers = new ArrayList<Integer>();
    }

    public void addPlayer(Player player) {
        players.add(player);
    }

    public void removePlayer(Player player) {
        players.remove(player);
    }

    public void playerAnswered() {
        if (++answerCounter == this.players.size()) {
            updateQuestion();
            answerCounter = 0;
        }
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
