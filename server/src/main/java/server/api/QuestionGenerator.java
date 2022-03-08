package server.api;

import commons.Activity;
import commons.Question;
import org.springframework.data.util.Pair;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class QuestionGenerator {

    public static Pair<Question, List<Integer>> generateComparisonQuestion(List<Activity> activities) {
        Question q = new Question("Which activity takes the most energy?", "N/A",
                Question.QuestionType.COMPARISON);

        int answerIndex = 0;
        int maxUsage = Integer.MIN_VALUE;

        for (int i = 0; i < activities.size(); ++i) {
            Activity a = activities.get(i);
            q.addAnswerOption(a.title);
            if (Integer.parseInt(a.consumption) > maxUsage) {
                answerIndex = i;
                maxUsage = Integer.parseInt(a.consumption);
            }
        }

        return Pair.of(q, List.of(answerIndex));
    }

    public static Pair<Question, List<Integer>> generateMultipleChoiceQuestion(Activity activity) {
        Question q = new Question("Guess how much energy the following activity takes\n" + activity.title,
                activity.imagePath, Question.QuestionType.MULTIPLE_CHOICE);

        List<Integer> answerOptions = new ArrayList<Integer>();
        answerOptions.add(Integer.parseInt(activity.consumption));
        q.addAnswerOption(answerOptions.get(0) + "Wh");

        Random rng = new Random();
        for (int i = 0; i < 3; ++i) {
            answerOptions.add(rng.nextInt(10000));
            q.addAnswerOption(answerOptions.get(i + 1) + "Wh");
        }

        return Pair.of(q, answerOptions);
    }

    public static Pair<Question, List<Integer>> generateEstimationQuestion(Activity activity) {
        Question q = new Question("Guess how much Wh of energy the following activity takes\n" + activity.title,
                activity.imagePath, Question.QuestionType.RANGE_GUESS);

        return Pair.of(q, List.of(Integer.parseInt(activity.consumption)));
    }

    public static Pair<Question, List<Integer>> generateEquivalenceQuestion(Activity a, List<Activity> other) {
        Question q = new Question(
                "What could you do instead of the following activity to use the same energy?\n" +
                a.title, a.imagePath, Question.QuestionType.EQUIVALENCE);

        int diff = Integer.MAX_VALUE;
        int closestIndex = 0;

        for (int i = 0; i < other.size(); ++i) {
            Activity act = other.get(i);
            int curDiff = Math.abs(Integer.parseInt(a.consumption) - Integer.parseInt(act.consumption));
            if (curDiff < diff) {
                diff = curDiff;
                closestIndex = i;
            }
        }

        return Pair.of(q, List.of(closestIndex));
    }
}