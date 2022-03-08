package commons;

import commons.Activity;
import commons.Question;
import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class QuestionGenerator {

    private static Pair<Question, List<Integer>> generateComparisonQuestion(List<Activity> activities) {
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

    private static Pair<Question, List<Integer>> generateMultipleChoiceQuestion(Activity activity) {
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

    private static Pair<Question, List<Integer>> generateEstimationQuestion(Activity activity) {
        Question q = new Question("Guess how much Wh of energy the following activity takes\n" + activity.title,
                activity.imagePath, Question.QuestionType.RANGE_GUESS);

        return Pair.of(q, List.of(Integer.parseInt(activity.consumption)));
    }

    private static Pair<Question, List<Integer>> generateEquivalenceQuestion(Activity a, List<Activity> other) {
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

    public static Pair<Question, List<Integer>> generateQuestion() {
        Random rng = new Random();
        switch (rng.nextInt(4)) {
            case 0:
                return generateComparisonQuestion(List.of(
                        new Activity("Comp 1", "1000", "img1", "no"),
                        new Activity("Comp 2", "4000", "img2", "no"),
                        new Activity("Comp 3", "2000", "img3", "no"),
                        new Activity("Comp 4", "1500", "img4", "no")
                ));
            case 1:
                return generateMultipleChoiceQuestion(new Activity(
                        "MC 1", "570", "img1", "no"
                ));
            case 2:
                return generateEquivalenceQuestion(new Activity(
                    "Eq 1", "1000", "img1", "no"
                ), List.of(
                    new Activity("Eq 2", "4000", "img2", "no"),
                    new Activity("Eq 3", "2000", "img3", "no"),
                    new Activity("Eq 4", "1500", "img4", "no")
                ));
            default:
                return generateEstimationQuestion(new Activity(
                        "Est 1", "440", "img1", "no"
                ));
        }
    }
}