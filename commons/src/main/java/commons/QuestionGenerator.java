package commons;

import org.apache.commons.lang3.tuple.Pair;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class QuestionGenerator {

    /**
     * Generate a comparison style question
     * @param activities List of activities to compare
     * @return The question and the list of expected answers
     */
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

    /**
     * Generate a multiple choice type question
     * @param activity The activity to guess the consumption of
     * @return A question and the list of expected answers
     */
    private static Pair<Question, List<Integer>> generateMultipleChoiceQuestion(Activity activity) {
        Question q = new Question("Guess how much energy the following activity takes\n" + activity.title,
                activity.imagePath, Question.QuestionType.MULTIPLE_CHOICE);

        q.addAnswerOption(Integer.parseInt(activity.consumption) + " Wh");

        Random rng = new Random();
        for (int i = 0; i < 3; ++i) {
            q.addAnswerOption(rng.nextInt(10000) + " Wh");
        }

        return Pair.of(q, List.of(0));
    }

    /**
     * Generate an estimation style question
     * @param activity The activity with the consumption to be estimated
     * @return A question and the list of expected answers
     */
    private static Pair<Question, List<Integer>> generateEstimationQuestion(Activity activity) {
        Question q = new Question("Guess how much Wh of energy the following activity takes\n" + activity.title,
                activity.imagePath, Question.QuestionType.RANGE_GUESS);

        return Pair.of(q, List.of(Integer.parseInt(activity.consumption)));
    }

    /**
     * Generate an equivalence style question
     * @param a The activity being compared to alternatives
     * @param other Alternative activities instead of the first one
     * @return A question and the list of expected answers
     */
    private static Pair<Question, List<Integer>> generateEquivalenceQuestion(Activity a, List<Activity> other) {
        Question q = new Question(
                "What could you do instead of the following activity to use the same energy?\n" +
                a.title, a.imagePath, Question.QuestionType.EQUIVALENCE);

        int diff = Integer.MAX_VALUE;
        int closestIndex = 0;

        for (int i = 0; i < other.size(); ++i) {
            Activity act = other.get(i);
            q.addAnswerOption(act.title);
            int curDiff = Math.abs(Integer.parseInt(a.consumption) - Integer.parseInt(act.consumption));
            if (curDiff < diff) {
                diff = curDiff;
                closestIndex = i;
            }
        }

        return Pair.of(q, List.of(closestIndex));
    }

    /**
     * Generate a question from the 4 basic types
     * @return The question and the list of expected answers
     */
    public static Pair<Question, List<Integer>> generateQuestion() {
        List<Question.QuestionType> choices = Arrays.stream(Question.QuestionType.values()).filter(
                qt -> qt != Question.QuestionType.UNKNOWN).toList();

        Random rng = new Random();
        return generateQuestion(choices.get(rng.nextInt(choices.size())));
    }

    /**
     * Generate a question of the provided type
     * @param type The type of the question to generate
     * @return The question and the list of expected answers
     */
    public static Pair<Question, List<Integer>> generateQuestion(Question.QuestionType type) {
        switch (type) {
            case COMPARISON:
                return generateComparisonQuestion(List.of(
                        new Activity("Comp 1", "1000", "img1", "no"),
                        new Activity("Comp 2", "4000", "img2", "no"),
                        new Activity("Comp 3", "2000", "img3", "no"),
                        new Activity("Comp 4", "1500", "img4", "no")
                ));
            case MULTIPLE_CHOICE:
                return generateMultipleChoiceQuestion(new Activity(
                        "MC 1", "570", "img1", "no"
                ));
            case EQUIVALENCE:
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